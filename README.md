# Morendo

Morendo is a RETE inference engine written in Java. It reads rules in the CLIPS language,
matches them against facts and Java objects, and fires the rules whose conditions hold. It is
meant for research, learning and experimentation, and it is small enough to read: the whole
engine is one jar that depends only on Log4j.

Beyond plain CLIPS it offers Java beans as facts, rule properties (no-agenda rules, effective and
expiration dates, temporal activation, modification actions), `defquery` and graph queries, MOLAP
cubes with a `cubequery` conditional element, temporal facts, the `only` and `multiple`
conditional elements, a rule cost function, and a service module that pools engines for server
use. Ordered facts are deliberately not supported: every fact has a template.

## Origins and authors

Morendo was written by **Peter Lin** as the "morendo" branch of his Jamocha project, itself
descended from Sumatra, and published on GitHub at
[github.com/woolfel/morendo](https://github.com/woolfel/morendo) in 2021, where **Dave Woodman**
contributed as well. The original README acknowledged Dr. Charles Forgy, Paul Haley, Gary Riley
and Ernest Friedman-Hill: Morendo is a clean-room RETE implementation, but the lessons of OPS5,
CLIPS, Jess and other engines shaped it. It is released under the Apache License 2.0 (see
`LICENSE`).

The original README is kept as [README2.md](README2.md).

## This repository

This repository, [github.com/blakemcbride/morendo](https://github.com/blakemcbride/morendo), is a
fork that was heavily modified and documented by **Blake McBride** using
[Claude Code](https://claude.com/claude-code), Anthropic's agentic coding tool, in September 2026.
The engine's design and algorithms are Peter Lin's; the modernization, the bug fixes, the
language additions and the documentation described below are the fork's.

## What changed from the original, in broad strokes

1. **A modern build and code base.** Java 21; the Ant build, Eclipse files and committed jars
   were replaced by a one-command build (`./bld`) that fetches its libraries and generates the
   parser; the package root moved from `org.jamocha` to `org.morendo`; Log4j 1, JUnit 4 and
   `javax` were replaced by Log4j 2, JUnit 6 and Jakarta; the source compiles with zero
   `-Xlint:all` warnings, is formatted with google-java-format, checked by SpotBugs, carries
   jspecify nullness annotations on the public API, and is built and tested by GitHub CI.
2. **Correctness.** Golden characterization tests pin every sample's output and firing order, and
   a long bug-fix pass repaired the join, query, agenda and compiler problems those tests and the
   manual's transcripts exposed (not-equal joins, rules opening with `not`, predicate constraints
   with nested calls or globals, shared predicate nodes, query parameters, rule removal, and
   more).
3. **The CLIPS language filled in.** Nested calls as slot values; multi-expression `deffunction`
   bodies; `progn`, `while`, `loop-for-count`, `foreach`, `break`, `return` and `halt`; `deffacts`
   and a CLIPS `reset`; the fact accessors; `agenda` and `refresh`; `format`, file routers with
   `open`, `close`, `readline` and `read`; the type functions; `subseq$`, `replace$`,
   `delete-member$`, `gensym`, `seed`, `system`; `=`, `<>` and `!=` in predicates; the CLIPS form
   of `defglobal`; and the `or` and `forall` conditional elements and return-value constraints.
4. **Many engines in one process.** The service module's engine pools are thread-safe and block
   when exhausted, a request's facts are retracted when its context closes, profiling counters
   are per engine, and an engine no longer writes a working directory into the current
   directory.
5. **Documentation.** An 87-page user manual and tutorial (`manual/morendo.pdf`) whose shell
   transcripts are real output replayed against every build, a developer guide (`CLAUDE.md`),
   and the plan that drove the modernization (`UpgradePlan.md`).

The version is 2.1.0.

## Where things are

| Path | What |
|---|---|
| `src/core/` | the engine: RETE nodes, compiler, functions, the CLIPS grammar (`src/core/javacc/clips.jj`) |
| `src/shell/`, `src/gui/` | the interactive shell (JLine) and the Swing GUI with the network viewer |
| `src/service/`, `src/messaging/` | the embedding service with engine pools and a servlet variant; the Jakarta Messaging client |
| `src/test/` | JUnit tests, golden files and scenario scripts |
| `samples/`, `benchmark/manners/` | small rule files per feature; the Manners benchmark |
| `manual/` | the LaTeX manual, `morendo.pdf`, and `replay.py`, which re-runs its transcripts |
| `doc/`, `classdiagrams/` | Peter Lin's design papers and diagrams |
| `builder/Tasks.java` | the whole build definition |

## Building, running and using it

- [BUILDING.md](BUILDING.md) explains how to build, test, check and package Morendo.
- [USAGE.md](USAGE.md) is the quick start: the shell, rule files, and embedding the engine in
  Java.
- `manual/morendo.pdf` is the full manual and tutorial.
- `CLAUDE.md` describes the architecture and the conventions for changing the code.
