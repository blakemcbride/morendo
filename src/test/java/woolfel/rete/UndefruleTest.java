package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** Removing a rule takes its activations off the agenda. */
public class UndefruleTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void removesThePendingActivationsOfTheRule() {
        Rete engine = new Rete();
        try {
            load(
                    engine,
                    """
                    (deftemplate a (slot x))
                    (defrule keep (a (x ?x)) => (printout t "keep" crlf))
                    (defrule drop (a (x ?x)) => (printout t "drop" crlf))
                    (defrule either (or (a (x 1)) (a (x 2))) => (printout t "either" crlf))
                    (assert (a (x 1)))
                    (assert (a (x 2)))
                    (undefrule drop)
                    (undefrule either)
                    """);
            assertEquals(2, engine.fire(), "only keep fires, once per fact");
        } finally {
            engine.close();
        }
    }
}
