package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import woolfel.examples.model.Account;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** set-member resolves variables and calls, and converts the value to the property's type. */
public class SetMemberTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void takesVariablesAndCallsAndConvertsNumbers() throws Exception {
        Rete engine = new Rete();
        try {
            engine.declareObject(Account.class);
            load(
                    engine,
                    """
                    (defrule older
                      ?a <- (woolfel.examples.model.Account (first "ann") (age ?age&:(< ?age 40)))
                    =>
                      (bind ?next (+ ?age 10))
                      (set-member ?a age ?next)
                      (set-member ?a last (str-cat "lee-" ?next))
                      (set-member ?a middle "m"))
                    """);
            Account ann = new Account();
            ann.setFirst("ann");
            ann.setAge(34);
            engine.assertObject(ann, null, false, true);
            assertEquals(1, engine.fire());
            assertEquals(44, ann.getAge());
            assertEquals("lee-44", ann.getLast());
            assertEquals("m", ann.getMiddle());
        } finally {
            engine.close();
        }
    }
}
