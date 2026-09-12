# morendo
RETE inference rule engine written in Java inspired by CLIPS and JESS. Morendo is meant for research, learning and experimentation.

## Features
Morendo speaks the CLIPS language and is largely compatible with rules written for CLIPS and JESS:
templates, rules with `not`, `exists`, `and`, `or`, `forall` and `test`, predicate and return-value
constraints, `deffacts` and `reset`, functions with multi-expression bodies, globals, modules with
focus and auto-focus, loops, `format` and file routers, and an agenda with salience, strategies,
`agenda`, `refresh` and `halt`. Morendo does not support ordered facts: although they are useful for
quick prototyping, they lead to bad habits, and real projects built on them have had to be
rewritten. Design your model and use Java objects or deftemplates. Appendix C of the manual lists
the remaining differences from CLIPS.

**Java objects as facts** Any Java bean can be declared as a template; its instances are asserted as
shadow facts and rules match their properties directly. **Java macros** eliminate the cost of
reflection when reasoning over POJOs.

**MOLAP** multi-dimensional OLAP cubes are commonly used in financial applications. Morendo has MOLAP
cubes and cube queries to make it easier to reason over financial data; there are examples in the
samples folder.

**Network topology cost** calculates the cost of a rule: the function counts the nodes the rule
needs to estimate a relative cost (an alpha node for a literal constraint costs 1, a join node 4).
Rules with a higher cost use more memory, so the cost function gives you more information for
design decisions. **Profiling** times the engine's operations per engine.

**Graph query** provides a generic way to load concept graphs and use them in rules.

Morendo provides some **temporal logic features** to make it easier to reason over temporal data and
define temporal patterns. Temporal activation checks whether the facts are expired before adding an
activation to the agenda; expired facts are retracted instead.

**Event-driven rules** Applications that process event streams can set the rule property `no-agenda`
to true, which skips the agenda and executes the rule's actions immediately.

Two features from Haley Enterprise, **only and multiple**, add some second-order logic support.

**Engines for servers** The service module keeps a pool of engines per rule application,
initialized from a JSON configuration, and hands one to each request thread; an engine itself is
driven from one thread at a time.

## Building and running
Requires JDK 21. The build is driven by `bld` (see `builder/Tasks.java`); the first build
downloads the dependencies into `libs/`.

```
./bld build      # generate the CLIPS parser, compile every module (bld.cmd on Windows)
./bld test       # run the test suite
./bld lint | format-check | spotbugs   # the checks CI runs (javac -Xlint, google-java-format, SpotBugs)
./bld dist       # target/morendo-<version>.zip with the module jars, libraries, launcher and samples
./morendo -shell # interactive shell from a checkout; ./morendo -gui for the Swing GUI
```

The build produces one jar per module: `morendo-core` (the engine, needs only Log4j),
`morendo-shell` (the launcher and interactive shell, JLine), `morendo-gui` (Swing GUI and
network viewer), `morendo-service` (embedding service and servlet, Jackson and Jakarta Servlet),
`morendo-messaging` (JMS client and agent functions, Jakarta Messaging) and `morendo-examples`
(the sample beans). Embedders need only `morendo-core` plus Log4j and create an
`org.morendo.rete.Rete` (the package root was `org.jamocha` before 2.0.0); the optional jars add
their functions to the engine automatically when they are on the classpath.

The user manual and tutorial is `manual/morendo.pdf` (LaTeX sources in `manual/`, built with
`make` there; `python3 manual/replay.py` re-runs every shell transcript in it against the build).

Inside the shell: `(batch samples/only/only_1.clp)`, `(facts)`, `(fire)`, `(exit)`. The shell has
line editing and history (JLine); constructs can be typed over several lines. Logging goes to
stderr at WARN; see `src/core/resources/log4j2.xml` for the options.

## Acknowledgements
Morendo wouldn't be possible without the work by Dr. Forgy, Paul Haley, Gary Riley and Ernest Friedman-Hill. Even though morendo is a clean room implementation of RETE, the lessons learned from OPS5, CLIPS, JESS and half dozen other RETE rule engines influenced the implementation. The rule engine is open source, so that anyone can learn from it.
