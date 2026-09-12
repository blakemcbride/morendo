package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Deffacts;
import org.morendo.rete.Fact;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** deffacts, the CLIPS reset and the functions around them. */
public class DeffactsResetTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    static Rete engine() {
        Rete engine = new Rete();
        load(
                engine,
                "(deftemplate person (slot name) (slot age))"
                        + "(deffacts people \"the starting facts\""
                        + "  (person (name \"ann\") (age 30))"
                        + "  (person (name \"bob\") (age 40)))"
                        + "(deffacts more (person (name \"cy\") (age (+ 40 10))))");
        return engine;
    }

    @Test
    public void deffactsAreRecordedNotAsserted() {
        Rete engine = engine();
        try {
            assertEquals(1, engine.getAllFacts().size(), "only the initial fact before reset");
            Deffacts people = engine.getCurrentFocus().getDeffacts("people");
            assertNotNull(people);
            assertEquals("the starting facts", people.getComment());
            assertEquals(2, people.getFacts().size());
            assertEquals(2, engine.getCurrentFocus().getAllDeffacts().size());
        } finally {
            engine.close();
        }
    }

    @Test
    public void resetAssertsDeffactsAndRestartsNumbering() {
        Rete engine = engine();
        try {
            load(engine, "(assert (person (name \"zed\") (age 1)))");
            assertEquals(2, engine.getAllFacts().size());
            engine.resetAll();
            List<Fact> facts = engine.getAllFacts();
            assertEquals(4, facts.size(), "initial fact plus three deffacts facts");
            assertNotNull(engine.getFactById(1), "numbering restarts at the initial fact");
            Fact cy = engine.getFactById(4);
            assertNotNull(cy);
            assertEquals("cy", cy.getSlotValue(cy.getSlotId("name")));
            assertEquals(50L, ((Number) cy.getSlotValue(cy.getSlotId("age"))).longValue());
            assertNull(engine.getFactById(5), "the fact asserted by hand is gone");
            engine.resetAll();
            assertEquals(4, engine.getAllFacts().size(), "reset is repeatable");
        } finally {
            engine.close();
        }
    }

    @Test
    public void resetReactivatesRules() {
        Rete engine = engine();
        try {
            load(engine, "(defrule adult (person (age ?a&:(>= ?a 40))) => )");
            load(engine, "(defrule no-lhs => )");
            engine.resetAll();
            assertEquals(3, engine.getCurrentFocus().getActivationCount(), "bob, cy and no-lhs");
            assertEquals(3, engine.fire());
            engine.resetAll();
            assertEquals(3, engine.fire(), "the same rules fire again after the next reset");
        } finally {
            engine.close();
        }
    }

    @Test
    public void undeffactsAndClear() {
        Rete engine = engine();
        try {
            load(engine, "(undeffacts more)");
            assertNull(engine.getCurrentFocus().getDeffacts("more"));
            engine.resetAll();
            assertEquals(3, engine.getAllFacts().size());
            load(engine, "(clear)");
            assertTrue(engine.getCurrentFocus().getAllDeffacts().isEmpty(), "clear drops deffacts");
            assertFalse(engine.getAllFacts().isEmpty(), "the initial fact is back");
        } finally {
            engine.close();
        }
    }
}
