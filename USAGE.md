# Using Morendo

The full reference is the manual, `manual/morendo.pdf`: a tutorial in Part I, the extensions in
Part II, the shell, GUI and Java API in Part III, and every function and the grammar in the
appendices. This page is the short version.

## The shell

Build first (see [BUILDING.md](BUILDING.md)), then start the shell and type CLIPS:

```
$ ./morendo -shell
Morendo> (deftemplate person (slot name) (slot age))
true
Morendo> (defrule adult (person (name ?n) (age ?a&:(>= ?a 18)))
  => (printout t ?n " is an adult" crlf))
true
Morendo> (assert (person (name "ann") (age 34)))
2
Morendo> (fire)
ann is an adult
1
Morendo> (exit)
```

`fire` is CLIPS's `run`. Input is collected until the parentheses balance, so constructs can be
typed over several lines; the shell has line editing and history. Useful commands: `(facts)`,
`(agenda)`, `(rules)`, `(templates)`, `(watch rules)`, `(ppdefrule name)`, `(reset)`, `(clear)`.

## Rule files

Put constructs in a file and load it with `batch`; the `samples/` directory has a small file per
feature and `benchmark/manners/` the Manners benchmark:

```
Morendo> (batch samples/only/only_1.clp)
true
Morendo> (fire)
spiderman save the day
1
```

Comments in rule files are `;;`. A file that declares its starting facts in a `deffacts` and ends
with `(reset)` and `(fire)` runs the same way every time it is loaded.

## Embedding the engine in Java

Only `morendo-core` and Log4j are needed on the class path. An engine is driven from one thread at
a time; a program serving many threads gives each its own engine or uses the service module's
pool.

```java
import org.morendo.rete.Rete;

Rete engine = new Rete();
try {
    engine.loadRuleset("rules.clp");                     // templates, rules, deffacts
    engine.declareObject(Order.class);                    // a Java bean as a template
    engine.assertObject(new Order("A-1", 250.0), null, false, true);
    int fired = engine.fire();
} finally {
    engine.close();
}
```

Rules match the bean's properties by name, `(Order (total ?t&:(> ?t 100)))`, and
`engine.addPrintWriter(name, writer)` captures what the rules print. The manual's Part III covers
listeners, temporal facts, writing functions in Java, driving the engine through its message
router, and the service module, which pools engines per rule application from a JSON
configuration (`samples/configuration/`).

## Where to look next

- Chapter 3 of the manual is a tutorial you can follow at the shell in twenty minutes.
- Appendix A lists every built-in function with its usage string; `(usage name)` prints one at
  the shell.
- Appendix C lists the differences from CLIPS, ordered facts foremost.
