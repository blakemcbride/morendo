/*
 * Copyright 2002-2006 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package woolfel.rete;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Defclass;
import org.morendo.rete.Deftemplate;
import org.morendo.rete.Slot;
import org.morendo.rete.ValueType;

import woolfel.examples.model.Account;
import woolfel.examples.model.TestBean2;

/**
 * @author Peter Lin
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Style - Code Templates
 */
public class DeftemplateTest {

    /**
     * Basic test of Defclass.createDeftemplate(String). the method uses TestBean2 to create a
     * Defclass.
     */
    @Test
    public void testCreateTemplateFromClass() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        assertNotNull(dtemp);
        System.out.println(dtemp.toPPString());
    }

    /**
     * the test creates 4 slots and uses them to create a deftemplate the method only test the
     * getNumberOfSlots method and toPPEString.
     */
    @Test
    public void testCreateTemplateFromSlots() {
        Slot[] slots = new Slot[4];
        slots[0] = new Slot();
        slots[0].setId(0);
        slots[0].setName("col1");
        slots[0].setValueType(ValueType.INT_PRIM);

        slots[1] = new Slot();
        slots[1].setId(1);
        slots[1].setName("col2");
        slots[1].setValueType(ValueType.DOUBLE_PRIM);

        slots[2] = new Slot();
        slots[2].setId(2);
        slots[2].setName("col3");
        slots[2].setValueType(ValueType.OBJECT);

        slots[3] = new Slot();
        slots[3].setId(3);
        slots[3].setName("col4");
        slots[3].setValueType(ValueType.LONG_PRIM);

        Deftemplate dtemp = new Deftemplate("template1", null, slots);
        assertNotNull(dtemp);
        assertEquals(4, dtemp.getNumberOfSlots());
        System.out.println(dtemp.toPPString());
    }

    /** method uses Account class to create a Defclass. the method tests toPPEString. */
    @Test
    public void testCreateTemplate2() {
        Defclass dc = new Defclass(Account.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("account");
        assertNotNull(dtemp);
        assertNotNull(dtemp.toPPString());
        System.out.println(dtemp.toPPString());
    }

    /** */
    @Test
    public void testCreateFactFromInstance() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("random1");
        bean.setAttr2(101);
        short s = 10001;
        bean.setAttr3(s);
        long l = 10101018;
        bean.setAttr4(l);
        bean.setAttr5(1010101);
        bean.setAttr6(1001.1001);
        org.morendo.rete.Fact fact = dtemp.createFact(bean, dc, 0);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
    }

    /** */
    @Test
    public void testSlotID() {
        Slot[] slots = new Slot[4];
        slots[0] = new Slot();
        slots[0].setId(0);
        slots[0].setName("col1");
        slots[0].setValueType(ValueType.INT_PRIM);

        slots[1] = new Slot();
        slots[1].setId(1);
        slots[1].setName("col2");
        slots[1].setValueType(ValueType.DOUBLE_PRIM);

        slots[2] = new Slot();
        slots[2].setId(2);
        slots[2].setName("col3");
        slots[2].setValueType(ValueType.OBJECT);

        slots[3] = new Slot();
        slots[3].setId(3);
        slots[3].setName("col4");
        slots[3].setValueType(ValueType.LONG_PRIM);

        Deftemplate dtemp = new Deftemplate("template1", null, slots);
        assertNotNull(dtemp);
        assertEquals(4, dtemp.getNumberOfSlots());
        System.out.println(dtemp.toPPString());
        Slot[] theslots = dtemp.getAllSlots();
        for (int idx = 0; idx < theslots.length; idx++) {
            Slot aslot = theslots[idx];
            assertEquals(idx, aslot.getId());
            System.out.println("slot id: " + aslot.getId());
        }
    }

    /**
     * method will use Account class to create a Defclass first. Once it has the deftemplate, we
     * check to make sure the slots have the correct slot id, which is the column id. this makes
     * sure that we can efficiently update facts using the slot id.
     */
    @Test
    public void testCreateTemplateSlot() {
        String acc = "account";
        Defclass dc = new Defclass(Account.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate(acc);
        assertNotNull(dtemp);
        assertNotNull(dtemp.toPPString());
        assertEquals(acc, dtemp.getName());
        System.out.println(dtemp.toPPString());
        Slot[] theslots = dtemp.getAllSlots();
        for (int idx = 0; idx < theslots.length; idx++) {
            Slot aslot = theslots[idx];
            assertEquals(idx, aslot.getId());
            System.out.println("slot id: " + aslot.getId());
        }
    }
}
