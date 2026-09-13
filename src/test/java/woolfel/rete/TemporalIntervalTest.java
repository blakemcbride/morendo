package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** A temporal element with interval-time collects its matches and releases them in batches. */
public class TemporalIntervalTest {

    static final String RULE =
            """
            (deftemplate sell (slot sym))
            (deftemplate buy (slot sym))
            (defrule pair
              (sell (sym ?s))
              (temporal (declare ?tv (relative-time 60) (interval-time 1)) (buy (sym ?s)))
            =>
              (printout t "pair " ?s crlf))
            """;

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void releasesTheBatchAfterTheIntervalWithoutAFunction() throws Exception {
        Rete engine = new Rete();
        try {
            load(engine, RULE + "(assert (sell (sym \"A\"))) (assert (buy (sym \"A\")))");
            assertEquals(0, engine.fire(), "nothing is released before the interval is over");
            Thread.sleep(1100);
            load(engine, "(assert (sell (sym \"B\")))");
            assertEquals(1, engine.fire(), "the next fact after the interval releases the batch");
        } finally {
            engine.close();
        }
    }

    @Test
    public void doesNotReleaseAMatchRetractedWhileWaiting() throws Exception {
        Rete engine = new Rete();
        try {
            load(
                    engine,
                    RULE
                            + "(assert (sell (sym \"A\"))) (bind ?b (assert (buy (sym \"A\"))))"
                            + " (retract ?b)");
            Thread.sleep(1100);
            load(engine, "(assert (sell (sym \"B\")))");
            assertEquals(0, engine.fire());
        } finally {
            engine.close();
        }
    }
}
