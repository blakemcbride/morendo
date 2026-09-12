# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Morendo is a RETE inference engine in Java that speaks the CLIPS rule language (a fork of the
Jamocha/Sumatra engine; the package root was `org.jamocha` until 2.0.0 and is now `org.morendo`). Deliberately unsupported:
ordered facts. Comments in `.clp` files are `;;` (a single `;` is a token in this grammar). Added on top of plain RETE: MOLAP cubes, graph queries, temporal facts/rules,
`only`/`multiple` conditional elements, no-agenda (event-driven) rules, fuzzy bindings, and a
rule cost function. Design notes for these live in `doc/*.pdf|odt` and `classdiagrams/`.

## Build and run

The build is Blake's `bld` tool: `./bld <task>` (`bld.cmd` on Windows) compiles and runs
`builder/Tasks.java`, which is the whole build definition (`builder/BuildUtils.java` is bld's
generic helper library; do not edit it). Requires JDK 21; the first build downloads the jars listed
in `Tasks.java` from Maven Central into `libs/` (git-ignored). Everything built goes under `target/`.

```sh
./bld build                 # download libs, generate the parser, compile, copy resources
./bld test                  # + compile tests, run every test class through the JUnit console launcher
./bld test woolfel.rete.SimpleJoinTest      # one test class
./bld golden-update [only_1,manners16]      # regenerate golden files (all, or some)
./bld jar | dist | javadoc  # target/morendo-<version>.jar / .zip / javadoc
./bld lint                  # javac -Xlint:all on src/main/java; must stay at 0 warnings (details in target/lint.txt)
./bld clean | realclean     # remove target/ (+ generated parser); + libs/
./bld list-tasks
./morendo -shell            # interactive shell from a checkout (or -gui); needs a prior build
```

Layout: one source root per module, `src/<module>/java` with resources in
`src/<module>/resources`, compiled to `target/<module>/classes` and packaged as
`target/morendo-<module>-<version>.jar`. The modules and what they may depend on (enforced by
compiling each against only its own dependencies, see `MODULES` in `Tasks.java`):

| module | packages | third-party |
|---|---|---|
| `core` | `rete`, `rule`, `parser`, `model`, `mapping`, `messagerouter`, `nn`, `fuzzy` | Log4j only |
| `examples` | `woolfel.examples.*` beans and the `org.morendo.sample.im` example | - |
| `messaging` | `messaging`, `messaging.functions`, `messaging.agent` | Jakarta JMS |
| `gui` | `gui`, `gui.visualisation`, `gui.functions` (Swing) | - |
| `service` | `service`, `service.servlet` | Jackson, Jakarta Servlet |
| `shell` | `org.morendo.Morendo`, `shell.Shell` (depends on `gui`) | JLine |

`src/core/javacc/clips.jj` is the grammar; tests are in `src/test/java` (goldens and scenario
scripts in `src/test/resources`) and compile against every module. The version is
`Constants.VERSION`; `Tasks.java` reads it for jar and zip names.

bld compiles only sources newer than their class files, so after changing a method or field
signature run `./bld clean test` to avoid stale-class errors. `./bld lint` compiles everything
(generated parser included) but reports only hand-written sources; the code base is at zero
warnings, so a change that introduces one should fix it rather than suppress it. The remaining
`@SuppressWarnings("unchecked")` mark genuine unchecked casts (mostly `Object`-typed memories). A failed task exits non-zero
(the `guard` wrapper in `Tasks.java`; bld itself would exit 0). `run` and `test` spawn the JVM
without a console, so the interactive shell must be started with `./morendo`, not `bld run`.

CI (`.github/workflows/ci.yml`) runs `./bld test` on JDK 21. Tests are JUnit 6 (Jupiter),
discovered by scanning `target/test-classes`: a class is a test when it has `@Test` methods
**and** its name matches the launcher's default filter (`*Test`, `*Tests` or `Test*`), so name
new test classes that way. The rest of `src/test` (`*Benchmark*`, `rulebenchmark`, `hashtest`,
`cube`, `sample`) are benchmarks, generators and examples with no `@Test` methods. Engine tests
live in `src/test/java/woolfel/rete`; `src/test/java/woolfel/examples/model` has the bean classes
(`Account`, `Hobby`, ...) that samples and tests assert as facts. File paths in tests and `.clp`
files are relative to the repo root. `InitServiceTest` and the two JMS sample tests are
`@Disabled` with the reason (the service package never builds applications from its JSON config;
the messaging sample's rule file is not in the repository).

### Golden (characterization) tests

`org.morendo.golden.GoldenSampleTest` runs every self-contained sample under `samples/` plus the
scenario scripts in `src/test/resources/scenarios/` (Manners 16 guests, MOLAP, graph query) with
`(watch rules)` on, and compares the printed output, the firing trace and template/rule/fact/node
counts with `src/test/resources/golden/<name>.txt`. This is the safety net for the modernization
work in `UpgradePlan.md`: any engine change must keep it green, or the golden diff must be reviewed
and regenerated on purpose with `./bld golden-update`. Dates and activation timestamps are masked,
so the files are stable across runs and platforms.

Shell usage (run from the repo root):

```sh
./morendo -shell
Morendo> (batch samples/only/only_1.clp)
Morendo> (facts)
Morendo> (fire)          # NOT (run); the function is named "fire"
Morendo> (exit)
```

Logging is Log4j 2, configured by `src/core/resources/log4j2.xml`: WARN and above to stderr,
`-Dmorendo.log.level=DEBUG` for more, `-Dlog4j2.configurationFile=classpath:log4j2-file.xml` to
also write `logs/morendo.log`. Loggers are obtained with `LogManager.getLogger(X.class)`;
there is no logging wrapper any more. `logs/`, `cache/`, `libs/` and `target/` are git-ignored.

### Parser generation

The CLIPS grammar is `src/core/javacc/clips.jj`. `./bld build` (via the `parser` task) runs
JavaCC 7.0.13 from `libs/tools/` and writes `CLIPSParser*.java`, `Token*.java`,
`ParseException.java`, `SimpleCharStream.java` into `src/core/java/org/morendo/parser/clips/`,
where that directory's `.gitignore` hides them. Never hand-edit generated files; edit `clips.jj`
and rebuild (JavaCC regenerates only when the grammar is newer; `./bld clean` removes them).
`ParserUtils.java` in the same package is hand-written. `src/core/javacc/clips-experimental.jj`
is an unused variant of the grammar.

## Architecture

### Everything is a Function

`org.morendo.rete.Rete` is the engine facade; it delegates to `TemplateRegistry` (declared classes
and templates), `FunctionRegistry` (functions, function groups, measures; also loads any
`FunctionGroup` listed in `META-INF/services/org.morendo.rete.FunctionGroup`, which is how the
`gui` module contributes `view` and the `messaging` module its messaging and agent functions) and
`EngineOutput`
(print writers and the message router). A `Rete` instance is not thread-safe: drive it from one
thread, or through the `MessageRouter` command thread as the shell and GUI do. `Rete.close()`
marks the engine closed, stops the router and runs registered close hooks; `(exit)` only
closes the engine (nothing calls `System.exit` except the GUI window). All shell/CLIPS-level
operations (`batch`, `build`, `eval`, `deftemplate`, `defrule`, `fire`, `assert`, ...) are
`Function` implementations:

- `Function`: `getName()`, `executeFunction(Rete, Parameter[])` returning a `ReturnVector`
  (`DefaultReturnVector` of `DefaultReturnValue` tagged with a `ValueType`), `getReturnType()`
  (a `ValueType`), `getParameter()`, `toPPString()`. Comparison operators are the `Operator`
  enum; both replaced int codes in `Constants`.
- Functions are bundled in `FunctionGroup`s (`functions/list/ListFunctions`, `functions/math/MathFunctions`,
  ...). Each group's `loadFunctions(engine)` calls `engine.declareFunction(f)`. All built-in groups are
  registered in `Rete.loadBuiltInFunctions()`. To add a built-in: write the class, add it to the right
  group's `loadFunctions`.
- Parameters arrive as `ValueParam` (literal), `BoundParam` (call `resolveBinding(engine)` first),
  `FunctionParam2` (nested call), `SlotParam`, etc. Read arguments with
  `params[i].getValue(engine, ValueType.OBJECT)`, never the engine-less `getValue()`: only the
  former resolves bindings and nested calls. `Parameter`/`ReturnValue`, `Condition` and
  `Constraint` are sealed hierarchies with final leaves, so a new parameter kind or conditional
  element is an explicit change to the closed set.

Even `Rete.loadRuleset()` and `Rete.build()` just invoke `BatchFunction` / `BuildFunction`.

### Input path

Text -> `CLIPSParser` (JavaCC) -> builds `Defrule`/`Defquery`/`GraphQuery`/`Deftemplate`/`Defcube` objects
or `Function` + `Parameter[]` calls -> executed against `Rete`. The shell (`shell/Shell`, JLine
line editing, collects input until the parentheses balance) does not call the parser directly: it
sends each expression through a `StringChannel` on `messagerouter/MessageRouter`, whose daemon
command thread takes it from a blocking queue, hands it to `CLIPSInterpreter`, and posts
`MessageEvent`s (COMMAND, ENGINE output, RESULT or ERROR) that the channel reads back. The
shell returns when the input ends or the engine is closed. `service/` wraps the same engine
for embedding (`RuleService` -> `RuleApplication` -> `EngineContext`, JSON config in
`samples/configuration`, plus a `servlet/` variant).

### Rule compilation (rule -> RETE nodes)

`rete/DefaultRuleCompiler.addRule(Rule)`:

1. `rule.resolveTemplates(engine)`, compute `Complexity`, optionally validate (`rule/TemplateValidation`).
2. For each `Condition`, `condition.getCompiler(ruleCompiler).compile(...)` builds the alpha side.
   `rete/compiler/CompilerProvider` is a singleton holding one `ConditionCompiler` per CE type:
   Object, Exist, Temporal, Test, And, CubeQuery, Only, Multiple. Constraints (`LiteralConstraint`,
   `AndLiteralConstraint`, `OrLiteralConstraint`, `BoundConstraint`, `PredicateConstraint`) map to the
   `compileConstraint` overloads producing `AlphaNode`, `AlphaNodeAnd/Or`, `AlphaNodePredConstr`,
   `NumericAlphaNode`, `NoMem*` nodes. Equal alpha nodes are shared between rules (not between queries).
   `CompilerProvider` keeps one set of condition compilers per `Rete` instance (a `WeakHashMap`
   keyed by engine); it used to be a JVM-wide singleton, which broke any second engine in a JVM.
3. `compileJoins` links `BaseJoin` nodes; then a `TerminalNode` is attached and actions compiled;
   the rule is added to its `Module`.

Adding a new conditional element touches: a `Condition` class in `org.morendo.rule`, a `ConditionCompiler`
in `rete/compiler` wired into all three `CompilerProvider.getInstance` overloads (rule, query, graph
query), join node classes in `rete/` *and* their `Query*` twins in `rete/query`, and a production in
`clips.jj`.

### Node hierarchy (`org.morendo.rete`)

- `RootNode` -> `ObjectTypeNode` per template -> alpha nodes (`BaseAlpha`, `BaseAlpha2`) -> `LIANode`
  (left input adapter) -> joins (`BaseJoin`) -> terminal.
- Join families: `HashedEqBNode`/`HashedNotEqBNode` (bound-variable joins), `NotJoin`, `ExistJoin`,
  `OnlyJoin`, `MultipleJoin`, `ZJBetaNode` (no bindings), `TestNode`/`NTestNode`, `PredicateBNode`,
  `CubeQueryBNode`, temporal nodes. `*Frst` = first-join variant (left input is the initial fact);
  `*Neq*`/`NotEq` = not-equal binding variants (recent bug fixes were here, for CEs with several
  not-equal slots).
- Terminal nodes: `TerminalNode2` (default, `LinkedActivation`), `TerminalNode3` (effective/expiration
  dates), `NoAgendaTNode` (fires immediately, no agenda), `MLTerminalNode` (modification logic).
- Only `Fact`, `Template`, the slot classes and the fact implementations are `Serializable`;
  nodes, compilers, rules, functions and the GUI are not.
- `Evaluate` compares slot values with pattern-matching switches: strings and booleans by text,
  numbers exactly as longs when both are integral and as doubles otherwise, temporal values by
  epoch millisecond, anything else by `equals`.
- Node memories are NOT stored in nodes. `WorkingMemory` (`DefaultWM`) owns them via the generic
  `getAlphaMemory / getBetaLeftMemory / getBetaRightMemory` (`<T> T`, keyed by node; the node that
  created a memory knows its type), plus facts, deffacts, defglobals, modules, cubes, and the
  `Agenda`. `Rete.newMap()` and friends are generic factories for those maps.
- `rete/query` mirrors the node set (`QueryRootNode`, `QueryObjTypeNode`, `QueryHashedEqJoin`, ...) for
  `defquery` / graph queries; `DefaultQueryCompiler` and `GraphQueryCompiler` reuse the same condition
  compilers through their `Query` overloads.

### Facts, templates, Java objects

- `Deftemplate` = unordered fact with `Slot`s; `Deffact` is a fact instance. Templates and rules live
  in CLIPS-style `Module`s (default `MAIN`, `Constants.MAIN_MODULE`).
- Java beans are used directly: `Rete.declareObject(Class)` introspects with `Defclass` and creates a
  template named after the class, so rules match `(woolfel.examples.model.Account (age ?a))`.
  Objects are asserted as shadow facts; `rete/macro` (`ReadMacro`/`WriteMacro`) is the optional
  non-reflective property access path.
- `TemporalFact`/`TemporalDeffact` carry effective/expiration times as epoch milliseconds; the
  public API (`Rete.assertTemporalObject`, `assertFact(TemporalFact, Instant, Instant)`) and the
  time functions (`now`, `eq-day`, `within-seconds`, `add-hours`, ...) use `java.time.Instant`.
  DATE slot values are Instants; `Evaluate` also accepts `java.util.Date`/`Calendar` from beans,
  and time strings are ISO-8601. `model/Graph|Node|Edge` are
  auto-declared for graph queries; `Cube`/`Defcube`/`Defdimension`/`Defmeasure` + `rete/measures`
  implement MOLAP.

### Agenda and firing

`Agenda` holds activations per `Module`; ordering is a `Strategy` (`rete/strategies`: depth, breadth,
recency). `Rete.fire()` / `fire(n)` drains the focused module. Rule properties (`salience`,
`auto-focus`, `no-agenda`, `remember-alpha`, `hashed-memory`, `temporal-activation`, `effective-date`,
`expiration-date`, `rule-version`, `chaining-direction`) are
parsed into `Rule` setters in `clips.jj` (`ruleBody()`).

### Other packages

`rete/fuzzy` is the FuzzyJ-inspired
`FuzzyBinding`; `gui/visualisation` and `gui/` are the Swing network viewer and GUI tabs;
`messaging/` is a Jakarta Messaging (JMS) client; `service/servlet` targets Jakarta Servlet;
`rete/util/IOUtilities.open` is the one place that turns a location string (URL, `classpath:`
resource or file path) into a stream; `rule/util/TopologyCostCalculation`
is the README's rule cost function. `benchmark/manners` holds the classic Manners benchmark rule files.
