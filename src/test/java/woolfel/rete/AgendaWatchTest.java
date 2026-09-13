package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/** The activation trace does not depend on profiling. */
public class AgendaWatchTest {

    @Test
    public void activationsAreTracedWhileProfiling() {
        Rete engine = new Rete();
        try {
            StringWriter console = new StringWriter();
            engine.addPrintWriter("console", console);
            engine.setProfile(Rete.Profile.ALL);
            engine.setWatch(Rete.Watch.ACTIVATIONS);
            String text =
                    "(deftemplate t1 (slot a)) (defrule r (t1 (a ?x)) => (printout t ?x crlf))"
                            + " (bind ?f (assert (t1 (a 1)))) (retract ?f)";
            engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
            String out = console.toString();
            assertTrue(out.contains("=> Activation: r"), out);
            assertTrue(out.contains("<= Activation: r"), out);
        } finally {
            engine.close();
        }
    }
}
