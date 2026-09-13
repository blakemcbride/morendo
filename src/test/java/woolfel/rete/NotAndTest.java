package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * (not (and ...)) beyond the golden scenario: what it rejects, and removing rules whose tuples do
 * not pass a pattern node of their own.
 */
public class NotAndTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void removedRulesStartingFromTheInitialFactStayInactive() {
        Rete engine = new Rete();
        try {
            load(
                    engine,
                    """
                    (deftemplate a (slot x))
                    (deftemplate b (slot x))
                    (defrule g (not (and (a (x ?x)) (b (x ?x)))) => (printout t "g" crlf))
                    (defrule f (forall (a (x ?x)) (b (x ?x))) => (printout t "f" crlf))
                    (assert (a (x 1)))
                    (assert (b (x 1)))
                    """);
            assertEquals(1, engine.fire(), "f holds, g does not");
            load(
                    engine,
                    """
                    (undefrule g)
                    (undefrule f)
                    (retract 3)
                    (assert (b (x 1)))
                    """);
            assertEquals(0, engine.fire(), "neither removed rule activates again");
        } finally {
            engine.close();
        }
    }

    @Test
    public void rejectsElementsItCannotNegate() {
        Rete engine = new Rete();
        try {
            StringWriter console = new StringWriter();
            engine.addPrintWriter("console", console);
            load(
                    engine,
                    """
                    (deftemplate a (slot x))
                    (defrule bad (not (and (a (x 1)) (exists (a (x 2))))) => (printout t "bad" crlf))
                    """);
            assertTrue(
                    console.toString().contains("(not (and ...)) takes patterns"),
                    console.toString());
            assertNull(engine.getCurrentFocus().findRule("bad"));
        } finally {
            engine.close();
        }
    }
}
