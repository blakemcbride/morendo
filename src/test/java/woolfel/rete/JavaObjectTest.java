package woolfel.rete;

import static org.junit.jupiter.api.Assertions.*;

import org.jamocha.rete.Rete;
import org.junit.jupiter.api.Test;

import woolfel.examples.model.Account4;

import java.util.ArrayList;

public class JavaObjectTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDeclareObject() {
        Rete engine = new Rete();
        engine.declareObject(Account4.class);
        Account4 acc = new Account4();
        ArrayList objs = new ArrayList();
        objs.add(acc);
        try {
            engine.assertObjects(objs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testAssertObject() {
        Rete engine = new Rete();
        engine.declareObject(Account4.class);
        engine.addPrintWriter("sysout", new java.io.PrintWriter(System.out));
        engine.loadRuleset("./samples/ruleset/java_example4.clp");
        int rules = engine.getCurrentFocus().getRuleCount();
        assertTrue(1 == rules);
        Account4 acc = new Account4();
        acc.setAccountId("acc1");
        ArrayList objs = new ArrayList();
        objs.add(acc);
        try {
            engine.assertObjects(objs);
            int fired = engine.fire();
            System.out.println("rules fired=" + fired);
            assertEquals(1, fired);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
