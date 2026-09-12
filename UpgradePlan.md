# Morendo Upgrade Plan

Goal: bring Morendo from its Java 5 / Ant / log4j 1.2 / JUnit 3 origins to current libraries and
idiomatic Java 21, without changing rule semantics. Java 21 is the target because it is the LTS in
use here and everything already compiles on it. Work is ordered so that each phase leaves the tree
buildable and the samples behaving identically.

## Current state (measured 2026-09-12)

| Area | Finding |
|---|---|
| Sources | 643 main, 95 test, 8 sample `.java` files; compiles clean on JDK 21 with `--release 21` |
| Build | Ant `build.xml` (stale `parser` target, stale jar Main-Class), Eclipse `.classpath`; `ant run-tests` needs an Ant optional jar that is not installed |
| Committed binaries | `lib/*.jar` 2.7 MB, `morendo.wasjar` 1.4 MB prebuilt jar, two stray `bin/*.class`; no `.gitignore` |
| log4j 1.2.14 | Only 3 files touch it, all in `org.jamocha.logging` (own `Logger` interface + `LogFactory`) |
| JUnit 4.1 | 24 JUnit 3 style `TestCase` classes, 2 files with `@Test`; stale expectations and missing fixture files repaired in Phase 0 |
| Jackson 2.12.3 | 7 files: `ObjectMapper`, `@JsonIgnore`, `TypeReference` (service package config) |
| `jms.jar` (javax.jms) | 3 files in `org.jamocha.messaging` (Topic publish/subscribe) |
| `servlet-api` (javax.servlet) | 7 files: `service/servlet/*`, `logging/ServletLogger`, `service/RuleServiceImpl` |
| `xpp3`, `jackson-dataformat-xml` | Unreferenced |
| JLine | Referenced by launch scripts; jar was deleted from `lib/` |
| JavaCC | Grammar `clips.jj`; generated parser (JavaCC 7.0.10, ~6.5k lines) is committed |
| `-Xlint:all` on main | 256 rawtypes, 140 serial, 114 cast, 45 this-escape, 12 deprecation (all `new URL(String)`), 10 lossy-conversions, 5 unchecked, 4 static, 1 fallthrough |
| Legacy idioms | 1316 `instanceof`, 236 `StringBuffer`, 130 `List<?>`, 124 `@SuppressWarnings`, 34 `synchronized`, 8 `Hashtable`, 2 `Vector`, 7 boxed-primitive constructors, 4 `StringTokenizer`, `java.util.Date` in 24 files |
| Dead code | `rete/util` custom `HashMap`/`AbstractMap`/`Map`/`Entry`/`Iterator`/`Value` and `CollectionsFactory` have no callers outside `rete/util` (only `StringDataTest` benchmarks them) |
| Java serialization | Actually used by `batch-objects`/`batch-static-objects` functions and `service/ClipsInitialData`; `Serializable` is also on nodes, rules, functions where it is not needed |
| Threads | Hand-rolled `Thread`s in `Morendo`, `MessageRouter`, `StreamChannelImpl`, `messaging/BasicClient`, two GUI panels |

## Phase 0: Safety net (before touching anything) - DONE 2026-09-12

Delivered: `org.jamocha.golden.GoldenSampleTest` (28 scenarios, goldens under
`src/test/resources/golden/`, scenario scripts under `src/test/resources/scenarios/`),
`org.jamocha.AllTests`, `ant test`, `.github/workflows/ci.yml`, `.gitignore`, the stale tests
repaired, fixture files added (`samples/join_sample1.clp`, `samples/data/data.dat`,
`samples/ruleset/java_example4.clp`), `samples/ruleset/sample1.clp` fixed. Suite: 131 tests green.

One engine change was unavoidable: `rete/compiler/CompilerProvider` was a JVM-wide singleton bound
to the first engine that compiled a rule, so every further `Rete` in the same JVM (including every
test after the first, and the service package's engine pools) compiled against the wrong network.
It is now one provider per engine. The goldens were generated in isolated JVMs before the change and
pass unchanged after it.

Engine bugs found while building the net (not fixed; candidates for Phase 3 or earlier):

- `Rete.getRulesFired()` only ever contains `null`: `fire()` calls `activation.clear()` (which nulls
  the rule) before `addRuleFired(activation.getRule())`.
- `MessageRouter.messageQueue` is an unsynchronized `LinkedList` shared between the engine thread
  and the router's command thread; it occasionally throws `ArrayIndexOutOfBoundsException` in the
  background thread. The command thread is also non-daemon and polls every 10 ms for the life of
  the JVM, one per `Rete` instance.
- A two-way join with a not-equal binding (`(guest (sex ?s)) (guest (sex ~?s) (hobby ?h))`) yields
  one activation where two are expected, and the Manners benchmark stops after `assign_first_seat`
  with both rule variants (`manners16.clp`, `manners10.clp`). The `manners16` golden pins the
  current (incomplete) run; expect to regenerate it when this is fixed.
- `benchmark/manners/manners16.clp` calls `(load ...)`, which is not a function; the others use
  `load-facts`.
- `service/RuleServiceImpl.createInstance` never converts the JSON application beans into
  `RuleApplication`s (the assignment is commented out), `RuleApplicationImpl.version` is never set
  from the bean, and `ClipsInitialData.loadData` loads from a cache file that is only written by an
  unused method, never from its `url`. `InitServiceTest` is kept but excluded from `AllTests`.

Original plan:


1. Characterization tests. Write a JUnit test that runs every self-contained `samples/**/*.clp`
   (`only`, `exists`, `time`, `defquery`, `graphquery`, `molap`) plus `benchmark/manners/manners16.clp`
   through `Rete.loadRuleset` and `fire()`, capturing `printout` via `Rete.addPrintWriter`, and
   compares against golden output files committed under `src/test/resources/golden/`. Also assert
   fact counts, rule counts, and `nextNodeId()` after loading, so RETE network shape changes are
   caught. Generate the goldens from the current code and review them once by hand.
2. Fix `samples/ruleset/sample1.clp` (unbalanced paren on the `age` slot) so it can join the suite.
3. Triage the existing tests: mark generators/benchmarks (`rulebenchmark/*`, `MemoryBenchmark*`,
   `RulesetBenchmark2`, `hashtest/*`) as non-tests; fix or delete tests that point at missing
   files; decide whether `ReteInitTest.testNodeId` (expects 3 nodes, gets 6) documents a real
   regression or a stale expectation, then fix one or the other.
4. Add a GitHub Actions workflow that builds and runs the suite on JDK 21. Everything after this
   point must keep it green.

## Phase 1: Build system and repository hygiene

Build tool: bld (decided). Install it with bld's `install` script, which adds `bld`, `bld.cmd` and
`builder/` (`Tasks.java`, `BuildUtils.java`, the three commons jars); `builder/Tasks.java` is the
only file to edit. Model the tasks on KISS.

1. Layout as bld expects it: `src/main/java`, `src/main/resources` (gui icons, `messages.properties`,
   `log4j2.xml`), `src/test/java`, `src/test/resources`, `libs/` for jars, `target/classes` for
   output. Move `src/samples` under `src/test/java` (it is only used by tests and docs).
2. Tasks in `Tasks.java`: `build` (javac `--release 21 -Xlint:all`, copy resources), `parser`
   (run JavaCC 7.x on `clips.jj` into a generated-sources directory, only when the grammar is
   newer than the output), `test` (compile `src/test`, run `org.junit.platform.console` or
   `JUnitCore` on `org.jamocha.AllTests`, fail the build on failures), `jar`, `dist` (zip with
   launcher scripts), `clean`, `realclean`. bld's "only do what is necessary" logic makes the
   parser step cheap.
3. Delete the committed generated parser (`CLIPSParser*.java`, `Token*.java`, `ParseException.java`,
   `SimpleCharStream.java`; the directory's `.gitignore` already lists them) after a one-time diff
   against a fresh JavaCC 7 run; move `clips-experimental.jj` out of the source tree.
4. Dependencies: list Maven Central URLs in `Tasks.java` (`bld libs` downloads them); delete `lib/`,
   `morendo.wasjar`, `bin/`, `build.xml`, `.classpath`, `.project`, `jamocha.sh`. Extend
   `.gitignore` with `target/`, `libs/` (downloaded jars) and `builder/*.class`.
5. Distribution: `bld dist` produces `morendo-<version>.zip` with a launcher replacing
   `morendo.sh/.bat/.ps1`; main class `org.jamocha.Morendo` (later `org.morendo.Morendo`). Publish
   as GitHub release assets instead of committing jars.
6. Version in one place (`Tasks.java` or a `version.properties` resource read by `Constants`);
   drop the hard-coded "6/2009-6/2021" shell banner date.
7. Update `.github/workflows/ci.yml` to run `./bld test`.

Verification: `./bld test` green, Phase 0 goldens unchanged, generated parser byte-identical to
the committed one.

## Phase 2: Library upgrades

Each item is independent; do them as separate commits.

1. Logging -> Log4j 2 directly (decided): `log4j-api` + `log4j-core`, 2.24 line or newer.
   Delete `org.jamocha.logging` (`Logger`, `LogFactory`, `DefaultLogger`, `EmbeddedLogger`,
   `ServletLogger`) and use `org.apache.logging.log4j.LogManager.getLogger()` at the call sites.
   Replace `PropertyConfigurator.configure("log4j.properties")` (which forces running from the
   repo root and writes `./logs/SystemOut.log`) with a `log4j2.xml` on the classpath that logs to
   the console at WARN by default; keep a file appender opt-in via a system property.
2. JUnit -> JUnit 5 (Jupiter, 5.13 line or newer). Step 1: add `junit-vintage-engine` so the 24
   `TestCase` classes run unchanged. Step 2: convert them (`extends TestCase` -> `@Test`,
   `assertEquals` static imports, `@BeforeEach`), then drop vintage. Use `@TempDir` for tests
   that write files and `@ParameterizedTest` for the golden-sample suite.
3. Jackson -> current 2.x (2.19 line or newer). API used is stable (`ObjectMapper`, `@JsonIgnore`,
   `TypeReference`); expect no source changes. Drop `jackson-dataformat-xml` and `xpp3`.
4. `javax.jms` -> `jakarta.jms:jakarta.jms-api` 3.1 and `javax.servlet` ->
   `jakarta.servlet:jakarta.servlet-api` 6.x. Both are import renames (`javax.` -> `jakarta.`)
   plus `ServletContextListener` registration in `web.xml` (`samples/configuration/web.xml`,
   `sax_web.xml`). Mark both as `provided`/optional so the core jar has no container deps
   (see Phase 4).
5. Shell line editing -> JLine 3 (3.30 line or newer) in `rete/Shell`, restoring what the deleted
   `jline-0.9.9.jar` provided: history, editing, multi-line paren-aware input.
6. Deprecated `new URL(String)` (12 sites, all "is this a file or a URL" checks in `functions/io`
   and `service`) -> `URI.create(s).toURL()` or, better, a small `IOUtilities.open(String)` helper
   that handles `file:`, `http(s):`, classpath, and plain paths in one place.

Verification: goldens unchanged; every jar in the `Tasks.java` dependency list is imported by something (`jdeps` on the built classes confirms it).

## Phase 3: Language modernization

Do the mechanical passes first (IntelliJ inspections + "Fix all" per inspection, one commit
each), then the structural ones. Run the golden suite after every commit.

### 3a. Mechanical (compiler and IDE driven, low risk)

- Raw types (256) and casts (114): generify the `List<?>` / `Map<?, ?>` returns
  (`Rete.getAllFacts`, `WorkingMemory.get*Memory`, `Condition.getNodes`, `Rule.getJoins`,
  `FunctionGroup.listFunctions`, ...) to real element types; remove the `@SuppressWarnings`
  (124) that then become unnecessary.
- `StringBuffer` -> `StringBuilder` (236), `Hashtable`/`Vector` -> `HashMap`/`ArrayList`,
  boxed constructors -> `valueOf`, `StringTokenizer` -> `String.split`, `Iterator` loops ->
  enhanced `for`, diamond operator, `List.of`/`Map.of` for constants, `String.formatted`/text
  blocks in `toPPString` implementations and tests.
- `serial` (140): drop `implements Serializable` from nodes, compilers, functions, GUI classes,
  and rules. Keep it on `Fact`, `Deffact`, `Template`, `Deftemplate`, `Slot`, the `service`
  data classes, and anything `batch-objects` / `ClipsInitialData` reads. Add
  `serialVersionUID` where it stays.
- `this-escape` (45): constructors that register listeners or start threads; move the escaping
  call into an `init()`/factory method.
- `lossy-conversions`, `fallthrough`, `static`: fix individually.
- `java.util.Date` (24 files, mostly `functions/time` and temporal facts) -> `java.time`
  (`Instant` for fact effective/expiration, `ZonedDateTime` for the `eq-day`/`within-*`
  functions). Keep `Date` overloads on the public `Rete.assertTemporalObject` API as thin
  adapters if embedders need them.
- Delete dead code: `rete/util` custom collections + `CollectionsFactory` (and `StringDataTest`),
  `rete/sc` (unreferenced), and the never-constructed, never-referenced `OrderedFactTypeNode`,
  `TemporalTNode`, `CountFact`, `ObjectFilter`. Re-check references before each deletion.

### 3b. Structural (needs judgment, medium risk)

- Pattern matching. `Evaluate.java` (303 `instanceof`, 54 static comparison methods) and the
  `compileConstraint` / `getCompiler` dispatch in `DefaultRuleCompiler`, `ObjectConditionCompiler`
  and `CLIPSParser` actions are `instanceof` ladders; rewrite with `instanceof` patterns and
  `switch` pattern matching.
- Sealed hierarchies. `Condition` (Object, Exist, Only, Multiple, Temporal, Test, And, Or,
  CubeQuery), `Constraint` (Literal, AndLiteral, OrLiteral, Bound, Predicate), `Parameter`
  (Value, Bound, Function, Slot, String, ShellBound) and `ReturnValue` are closed sets; make them
  `sealed` so the `switch` rewrites above are exhaustive and adding a CE type becomes a compile
  error until every compiler handles it.
- Records for immutable value types: `Binding`, `BindValue`, `CompositeIndex`, `EqHashIndex`,
  `NotEqHashIndex`, `HashIndex`, `DefaultReturnValue`, `Complexity` values. Caution: alpha-node
  sharing and hashed join memories depend on `equals`/`hashCode`; records change both. Keep the
  golden node-count assertions from Phase 0 as the guard.
- Enums for the `int` type codes in `Constants` (`STRING_TYPE`, `INTEGER_OBJECT`, ...), the
  `WATCH_*` / `PROFILE_*` flags, `CompileEvent` kinds, and `RuleProperty` names.
- Concurrency: replace the `Thread` subclasses in `Morendo`, `MessageRouter`,
  `StreamChannelImpl`, `BasicClient`, and the GUI panels with an `ExecutorService` /
  `BlockingQueue`; make `MessageRouter`'s command queue a `LinkedBlockingQueue` instead of
  `synchronized` + polling. Audit the 34 `synchronized` blocks in `Rete`/`DefaultWM` and either
  document the threading contract (engine is single-threaded; router serializes commands) or
  enforce it.
- `System.exit` (5 sites, including `ExitFunction`): throw or signal instead so embedders and
  tests can call `(exit)` safely.
- Function registration: `Rete.loadBuiltInFunctions()` hand-lists 18 groups and
  `declareFunction(String)` uses `Class.forName`; consider `ServiceLoader<FunctionGroup>` so
  optional modules (Phase 4) contribute functions without editing `Rete`.
- Split `Rete.java` (1.8k lines): extract the template/defclass registry, the function/measure
  registry, and the output-stream management into their own classes; keep `Rete` as the facade.

## Phase 4: Modules (optional dependencies stop being mandatory)

Split into separate jars (bld tasks or sub-projects) so the core has zero third-party runtime deps beyond Log4j 2:

- `morendo-core`: `rete`, `rule`, `parser`, `model`, `mapping`, `logging`, `messagerouter`
- `morendo-shell`: `Morendo` main, `Shell`, JLine
- `morendo-gui`: `gui`, `rete/visualisation` (Swing)
- `morendo-service`: `service` (Jackson) and `service/servlet` (Jakarta Servlet)
- `morendo-messaging`: `messaging` (Jakarta JMS)
- `morendo-examples`: samples and the `woolfel.examples.model` beans

The Swing GUI stays (decided); the JMS messaging package stays until decided otherwise.

## Phase 5: Tooling and docs

- Formatter: Spotless with a single style (Google Java Format or IntelliJ's), applied once as
  its own commit so later diffs stay readable. The tree currently mixes tabs and spaces.
- Static analysis: Error Prone (compile-time) and SpotBugs in CI. Nullness annotations
  (`org.jspecify`) on the public `Rete` API.
- Update `CLAUDE.md`, `README.md`, and the launch instructions for the new build; regenerate
  `classdiagrams/` if they are still wanted.
- Bump `Constants.VERSION` to 2.0.0 (decided): the API surface (generics, `java.time`, sealed
  types, Jakarta namespaces, package rename) is not source-compatible with 1.3.x.
- Final 2.0 step (decided): rename the package `org.jamocha` to `org.morendo` with IntelliJ's
  refactoring, then update the FQCN template names in `samples/**/*.clp`, the service JSON configs,
  `Constants`, the launcher main class, `CLAUDE.md` and `README.md`. Do it last so every earlier
  diff stays reviewable, and regenerate the goldens once (the `ruleset_sample1` output prints class
  names).

## Explicitly out of scope

- Java Platform Module System (`module-info.java`). Little benefit for a library with Swing and
  servlet optional parts; revisit after Phase 4 if desired.
- Rewriting the parser in a different tool (ANTLR). JavaCC 7 is maintained and the grammar works.
- Changing RETE algorithms or node implementations. Performance work is separate from this plan.

## Suggested order and effort

| Phase | Effort | Risk | Depends on |
|---|---|---|---|
| 0 Safety net | done | none | - |
| 1 Build system | 1 day | low | 0 |
| 2 Libraries | 1-2 days | low | 1 |
| 3a Mechanical | 2-3 days | low | 0 |
| 3b Structural | 1-2 weeks | medium | 3a |
| 4 Modules | 1-2 days | low | 2, 3a |
| 5 Tooling/docs | 1 day | none | any |

Phases 1, 2 and 3a can proceed in parallel branches once Phase 0 is in. Phase 3b should be one
subsystem at a time (parser actions, then `Evaluate`, then compilers, then nodes), each behind
the golden suite.

## Decisions (Blake, 2026-09-12)

1. Build tool: **bld** (Blake's own build system, as used by KISS).
2. Logging: **Log4j 2 directly**; drop the `org.jamocha.logging.Logger` abstraction.
3. **Keep the Swing GUI.** (The JMS messaging package was not decided; it stays until it is.)
4. **Target 2.0.0 with API breaks.**
5. **Rename `org.jamocha` to `org.morendo`**, as the final step of 2.0.
