package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Activation;
import org.morendo.rete.Rete;
import org.morendo.rule.Rule;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** The agenda listing, fire with a count, and refresh. */
public class AgendaRefreshTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    static Rete engine() {
        Rete engine = new Rete();
        load(
                engine,
                "(deftemplate n (slot v))"
                        + "(defrule high (declare (salience 200)) (n (v ?v)) => )"
                        + "(defrule low (n (v ?v)) => )"
                        + "(assert (n (v 1)))"
                        + "(assert (n (v 2)))");
        return engine;
    }

    static String describe(List<Activation> agenda) {
        StringBuilder sb = new StringBuilder();
        for (Activation act : agenda) {
            sb.append(act.getRule().getName()).append(act.getFacts()[0].getFactId()).append(' ');
        }
        return sb.toString().trim();
    }

    @Test
    public void listingIsInFiringOrderAndRemovesNothing() throws Exception {
        Rete engine = engine();
        try {
            List<Activation> agenda = engine.getCurrentFocus().listActivations();
            assertEquals("high3 high2 low3 low2", describe(agenda));
            assertEquals(4, engine.getCurrentFocus().getActivationCount());
            assertEquals(1, engine.fire(1));
            assertEquals("high2 low3 low2", describe(engine.getCurrentFocus().listActivations()));
            assertEquals(3, engine.fire());
        } finally {
            engine.close();
        }
    }

    @Test
    public void refreshReactivatesFiredMatches() throws Exception {
        Rete engine = engine();
        try {
            Rule low = engine.getCurrentFocus().findRule("low");
            assertEquals(0, engine.refreshRule(low), "nothing has fired yet");
            assertEquals(4, engine.fire());
            assertEquals(2, engine.refreshRule(low));
            assertEquals("low3 low2", describe(engine.getCurrentFocus().listActivations()));
            assertEquals(2, engine.fire());
            engine.retractById(2);
            assertEquals(1, engine.refreshRule(low), "the retracted match is forgotten");
            assertEquals("low3", describe(engine.getCurrentFocus().listActivations()));
            engine.retractById(3);
            assertNull(engine.getFactById(3));
            assertEquals(0, engine.getCurrentFocus().getActivationCount());
            assertEquals(0, engine.refreshRule(low));
        } finally {
            engine.close();
        }
    }
}
