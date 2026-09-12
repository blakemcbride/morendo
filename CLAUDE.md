# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Morendo is a RETE inference engine in Java that speaks the CLIPS rule language (a fork of the
Jamocha/Sumatra engine, so the package root is still `org.jamocha`). Deliberately unsupported:
ordered facts. Added on top of plain RETE: MOLAP cubes, graph queries, temporal facts/rules,
`only`/`multiple` conditional elements, no-agenda (event-driven) rules, fuzzy bindings, and a
rule cost function. Design notes for these live in `doc/*.pdf|odt` and `classdiagrams/`.

## Build and run

The build is Blake's `bld` tool: `./bld <task>` (`bld.cmd` on Windows) compiles and runs
`builder/Tasks.java`, which is the whole build definition (`builder/BuildUtils.java` is bld's
generic helper library; do not edit it). Requires JDK 21; the first build downloads the jars listed
in `Tasks.java` from Maven Central into `libs/` (git-ignored). Everything built goes under `target/`.

```sh
./bld build                 # download libs, generate the parser, compile, copy resources
./bld test                  # + compile tests, run org.jamocha.AllTests through JUnitCore
./bld test woolfel.rete.SimpleJoinTest      # one test class
./bld golden-update [only_1,manners16]      # regenerate golden files (all, or some)
./bld jar | dist | javadoc  # target/morendo-<version>.jar / .zip / javadoc
./bld clean | realclean     # remove target/ (+ generated parser); + libs/
./bld list-tasks
./morendo -shell            # interactive shell from a checkout (or -gui); needs a prior build
```

Layout: `src/main/java` (engine), `src/main/resources` (gui icons, `messages.properties`),
`src/main/javacc/clips.jj` (grammar), `src/test/java` (tests, sample beans, example code),
`src/test/resources` (goldens, scenario scripts). The version is `Constants.VERSION`;
`Tasks.java` reads it for jar and zip names.

bld compiles only sources newer than their class files, so after changing a method or field
signature run `./bld clean test` to avoid stale-class errors. A failed task exits non-zero
(the `guard` wrapper in `Tasks.java`; bld itself would exit 0). `run` and `test` spawn the JVM
without a console, so the interactive shell must be started with `./morendo`, not `bld run`.

CI (`.github/workflows/ci.yml`) runs `./bld test` on JDK 21. The suite is
`src/test/java/org/jamocha/AllTests.java`; only classes listed there are tests, the rest of
`src/test` (`*Benchmark*`, `rulebenchmark`, `hashtest`, `cube`, `sample`) are benchmarks,
generators and examples. Tests are JUnit 3 style (`extends TestCase`) under the JUnit 4.1 runner.
Engine tests live in `src/test/java/woolfel/rete`; `src/test/java/woolfel/examples/model` has the
bean classes (`Account`, `Hobby`, ...) that samples and tests assert as facts. File paths in tests
and `.clp` files are relative to the repo root. `InitServiceTest` is excluded from the suite
because the service package never builds applications from its JSON config.

### Golden (characterization) tests

`org.jamocha.golden.GoldenSampleTest` runs every self-contained sample under `samples/` plus the
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

Run from the repo root: `LogFactory` loads `log4j.properties` from the working directory, and log4j
then writes `./logs/SystemOut.log` (git-ignored, as are `cache/`, `libs/`, `target/`).

### Parser generation

The CLIPS grammar is `src/main/javacc/clips.jj`. `./bld build` (via the `parser` task) runs
JavaCC 7.0.13 from `libs/tools/` and writes `CLIPSParser*.java`, `Token*.java`,
`ParseException.java`, `SimpleCharStream.java` into `src/main/java/org/jamocha/parser/clips/`,
where that directory's `.gitignore` hides them. Never hand-edit generated files; edit `clips.jj`
and rebuild (JavaCC regenerates only when the grammar is newer; `./bld clean` removes them).
`ParserUtils.java` in the same package is hand-written. `src/main/javacc/clips-experimental.jj`
is an unused variant of the grammar.

## Architecture

### Everything is a Function

`org.jamocha.rete.Rete` is the engine facade. All shell/CLIPS-level operations (`batch`, `build`, `eval`,
`deftemplate`, `defrule`, `fire`, `assert`, ...) are `Function` implementations:

- `Function`: `getName()`, `executeFunction(Rete, Parameter[])` returning a `ReturnVector`
  (`DefaultReturnVector` of `DefaultReturnValue` tagged with `Constants.*_TYPE`), `getReturnType()`,
  `getParameter()`, `toPPString()`.
- Functions are bundled in `FunctionGroup`s (`functions/list/ListFunctions`, `functions/math/MathFunctions`,
  ...). Each group's `loadFunctions(engine)` calls `engine.declareFunction(f)`. All built-in groups are
  registered in `Rete.loadBuiltInFunctions()`. To add a built-in: write the class, add it to the right
  group's `loadFunctions`.
- Parameters arrive as `ValueParam` (literal), `BoundParam` (call `resolveBinding(engine)` first),
  `FunctionParam2` (nested call), `SlotParam`, etc.

Even `Rete.loadRuleset()` and `Rete.build()` just invoke `BatchFunction` / `BuildFunction`.

### Input path

Text -> `CLIPSParser` (JavaCC) -> builds `Defrule`/`Defquery`/`GraphQuery`/`Deftemplate`/`Defcube` objects
or `Function` + `Parameter[]` calls -> executed against `Rete`. The shell (`rete/Shell`) does not call
the parser directly: it opens a `StreamChannel` on `messagerouter/MessageRouter`, whose command thread
hands each command to `CLIPSInterpreter`, which parses and executes it. `service/` wraps the same engine
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

Adding a new conditional element touches: a `Condition` class in `org.jamocha.rule`, a `ConditionCompiler`
in `rete/compiler` wired into all three `CompilerProvider.getInstance` overloads (rule, query, graph
query), join node classes in `rete/` *and* their `Query*` twins in `rete/query`, and a production in
`clips.jj`.

### Node hierarchy (`org.jamocha.rete`)

- `RootNode` -> `ObjectTypeNode` per template -> alpha nodes (`BaseAlpha`, `BaseAlpha2`) -> `LIANode`
  (left input adapter) -> joins (`BaseJoin`) -> terminal.
- Join families: `HashedEqBNode`/`HashedNotEqBNode` (bound-variable joins), `NotJoin`, `ExistJoin`,
  `OnlyJoin`, `MultipleJoin`, `ZJBetaNode` (no bindings), `TestNode`/`NTestNode`, `PredicateBNode`,
  `CubeQueryBNode`, temporal nodes. `*Frst` = first-join variant (left input is the initial fact);
  `*Neq*`/`NotEq` = not-equal binding variants (recent bug fixes were here, for CEs with several
  not-equal slots).
- Terminal nodes: `TerminalNode2` (default, `LinkedActivation`), `TerminalNode3` (effective/expiration
  dates), `NoAgendaTNode` (fires immediately, no agenda), `TemporalTNode` (retracts expired facts
  instead of activating), `MLTerminalNode` (modification logic).
- Node memories are NOT stored in nodes. `WorkingMemory` (`DefaultWM`) owns them via
  `getAlphaMemory / getBetaLeftMemory / getBetaRightMemory` keyed by node, plus facts, deffacts,
  defglobals, modules, cubes, and the `Agenda`.
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
- `TemporalFact`/`TemporalDeffact` carry effective/expiration times; `model/Graph|Node|Edge` are
  auto-declared for graph queries; `Cube`/`Defcube`/`Defdimension`/`Defmeasure` + `rete/measures`
  implement MOLAP.

### Agenda and firing

`Agenda` holds activations per `Module`; ordering is a `Strategy` (`rete/strategies`: depth, breadth,
recency). `Rete.fire()` / `fire(n)` drains the focused module. Rule properties (`salience`,
`auto-focus`, `no-agenda`, `remember-alpha`, `hashed-memory`, `temporal-activation`, `effective-date`,
`expiration-date`, `rule-version`, `chaining-direction`) are
parsed into `Rule` setters in `clips.jj` (`ruleBody()`).

### Other packages

`rete/sc` holds statically-compiled node interfaces that nothing references; `rete/fuzzy` is the FuzzyJ-inspired
`FuzzyBinding`; `rete/visualisation` and `gui/` are the Swing network viewer and GUI tabs;
`messaging/` is a JMS client; `logging/LogFactory` wraps log4j 1.2; `rule/util/TopologyCostCalculation`
is the README's rule cost function. `benchmark/manners` holds the classic Manners benchmark rule files.
