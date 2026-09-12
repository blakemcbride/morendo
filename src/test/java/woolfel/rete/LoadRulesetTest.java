/*
 * Created on Aug 29, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package woolfel.rete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;
import org.morendo.rule.Defrule;

import java.util.Collection;
import java.util.Iterator;

// import org.morendo.rete.*;
// import org.morendo.rule.*;

/**
 * @author pete
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public class LoadRulesetTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testLoadOnlySample() {
        Rete engine = new Rete();
        engine.loadRuleset("./samples/only/only_1.clp");
        Collection rules = engine.getCurrentFocus().getAllRules();
        int count = rules.size();
        Iterator itr = rules.iterator();
        while (itr.hasNext()) {
            Defrule r = (Defrule) itr.next();
            System.out.println(r.toPPString());
        }
        assertEquals(1, count);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testLoadExistsSample() {
        Rete engine = new Rete();
        engine.loadRuleset("./samples/exists/exists_sample10.clp");
        Collection rules = engine.getCurrentFocus().getAllRules();
        int count = rules.size();
        Iterator itr = rules.iterator();
        while (itr.hasNext()) {
            Defrule r = (Defrule) itr.next();
            System.out.println(r.toPPString());
        }
        assertEquals(2, count);
    }
}
