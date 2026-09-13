package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/** Calls nested inside test elements, negated or not, read the variables of the patterns. */
public class NestedTestCallsTest {

    @Test
    public void nestedCallsSeeThePatternVariables() {
        Rete engine = new Rete();
        try {
            StringWriter console = new StringWriter();
            engine.addPrintWriter("console", console);
            String text =
                    """
                    (deftemplate o (slot id))
                    (deftemplate p (slot id))
                    (defrule predicate-own (o (id ?o&:(not (> (abs ?o) 100))))
                      => (printout t "e" ?o crlf))
                    (defrule predicate-join (o (id ?o)) (p (id ?q&:(not (> (abs ?q) ?o))))
                      => (printout t "f" ?o crlf))
                    (assert (p (id 0)))
                    (defrule negated-nested (o (id ?o)) (not (> (abs ?o) 100))
                      => (printout t "a" ?o crlf))
                    (defrule negated-or (o (id ?o)) (not (or (> ?o 100) (< ?o 0)))
                      => (printout t "b" ?o crlf))
                    (defrule two-levels (o (id ?o)) (test (not (> (abs ?o) 100)))
                      => (printout t "c" ?o crlf))
                    (defrule two-levels-and (o (id ?o)) (test (and (> ?o 0) (< (abs ?o) 200)))
                      => (printout t "d" ?o crlf))
                    (assert (o (id 1)))
                    (assert (o (id -150)))
                    """;
            engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
            assertEquals(6, engine.fire(), console.toString());
            String out = console.toString();
            for (String line : new String[] {"a1", "b1", "c1", "d1", "e1", "f1"}) {
                assertTrue(out.contains(line), out);
            }
            assertFalse(out.contains("-150"), out);
            assertFalse(out.contains("Exception"), out);
        } finally {
            engine.close();
        }
    }
}
