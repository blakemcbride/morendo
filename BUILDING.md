# Building Morendo

Morendo needs a JDK 21 and nothing else installed: the build tool, `bld`, is a small Java program
in `builder/`, and the first build downloads the libraries it needs into `libs/` from Maven
Central. The build definition is `builder/Tasks.java`; `builder/BuildUtils.java` is bld's generic
helper library.

## Tasks

Run `./bld <task>` from the repository root (`bld.cmd` on Windows).

| Task | What it does |
|---|---|
| `./bld build` | downloads the libraries, generates the CLIPS parser with JavaCC, compiles every module and copies its resources |
| `./bld test` | also compiles the tests and runs every test class through the JUnit console launcher |
| `./bld test woolfel.rete.SimpleJoinTest` | runs one test class |
| `./bld golden-update [name,...]` | regenerates the golden files of the characterization tests, all or some |
| `./bld lint` | `javac -Xlint:all` over every module; the code base is at zero warnings and must stay there |
| `./bld format`, `./bld format-check` | google-java-format (AOSP style) over all sources; the check fails when anything differs |
| `./bld spotbugs` | SpotBugs over the module classes; high-priority findings fail the task |
| `./bld jar`, `./bld dist`, `./bld javadoc` | `target/morendo-<version>.jar`, a `.zip` with the jars, libraries, launcher and samples, and the javadoc |
| `./bld clean`, `./bld realclean` | remove `target/` (and the generated parser); also `libs/` |
| `./bld list-tasks` | every task with a one-line description |

CI (`.github/workflows/ci.yml`) runs `format-check`, `test` and `spotbugs` on JDK 21. bld compiles
only sources newer than their class files, so after changing a method signature run
`./bld clean` before `./bld test` to avoid stale-class errors.

## Layout and modules

Each module has one source root, `src/<module>/java`, with resources in `src/<module>/resources`,
compiled to `target/<module>/classes` and packaged as `target/morendo-<module>-<version>.jar`.
Modules are compiled against their own dependencies only:

| Module | Contents | Third-party libraries |
|---|---|---|
| `core` | the engine, functions, grammar, MOLAP, temporal logic | Log4j only |
| `examples` | the sample beans and the instant-messaging example | none |
| `messaging` | Jakarta Messaging client and agent functions | Jakarta JMS |
| `gui` | Swing GUI and network viewer | none |
| `service` | embedding service, engine pools, servlet variant | Jackson, Jakarta Servlet |
| `shell` | the launcher and the interactive shell | JLine |

The grammar is `src/core/javacc/clips.jj`; the generated parser is written into
`src/core/java/org/morendo/parser/clips/` at build time and is not committed. Tests live in
`src/test/java`; the golden files and scenario scripts of the characterization tests are in
`src/test/resources`. The version is the single constant `Constants.VERSION`.

## The manual

`manual/` holds the LaTeX sources; `make` there builds `manual/morendo.pdf` with `pdflatex`
(three passes) and `make clean` removes the auxiliary files. Every shell transcript in the manual
is real output: after a build, `python3 manual/replay.py` replays them all against the engine and
prints the differences, and `python3 manual/replay.py --rewrite <chapter.tex>` re-records a
chapter.

## Running from a checkout

```
./morendo -shell     # the interactive shell (needs a prior build)
./morendo -gui       # the Swing GUI; -gui -shell opens both on one engine
```

Logging is Log4j 2, configured by `src/core/resources/log4j2.xml`: WARN and above to standard
error; `-Dmorendo.log.level=DEBUG` for more, and
`-Dlog4j2.configurationFile=classpath:log4j2-file.xml` to also write `logs/morendo.log`. To pass
JVM options, edit the `morendo` script or run `java -cp "libs/*" org.morendo.Morendo -shell`.
