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
import org.morendo.rete.AlphaNode;
import org.morendo.rete.ConversionUtils;
import org.morendo.rete.Defclass;
import org.morendo.rete.Deftemplate;
// import org.morendo.rete.ObjectTypeNode;
// import org.morendo.rete.Rete;
import org.morendo.rete.Operator;
import org.morendo.rete.Slot;

import woolfel.examples.model.TestBean2;

/**
 * @author Peter Lin
 *     <p>Simple test for slot to make sure it works correctly
 */
public class SlotTest {

    @Test
    public void testOneSlot() {
        // Rete engine = new Rete(); Unused
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        // TestBean2 bean = new TestBean2(); Unused
        Slot[] slts = dtemp.getAllSlots();
        // ObjectTypeNode otn = new ObjectTypeNode(1,dtemp,engine); Unused
        AlphaNode an = new AlphaNode(1);
        slts[0].setValue(ConversionUtils.convert(110));
        an.setOperator(Operator.EQUAL);
        an.setSlot(slts[0]);
        System.out.println("node::" + an.toString());
        assertNotNull(an.toString());
    }

    @Test
    public void testTwoSlots() {
        // Rete engine = new Rete(); Unused
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        // TestBean2 bean = new TestBean2(); Unused
        Slot[] slts = dtemp.getAllSlots();
        // ObjectTypeNode otn = new ObjectTypeNode(1,dtemp,engine); Unused
        AlphaNode an1 = new AlphaNode(1);
        AlphaNode an2 = new AlphaNode(1);

        slts[0].setValue("testString");
        slts[1].setValue(ConversionUtils.convert(999));

        an1.setSlot(slts[0]);
        an1.setOperator(Operator.EQUAL);
        System.out.println("node::" + an1.toPPString());
        assertNotNull(an1.toPPString());

        an2.setSlot(slts[1]);
        an2.setOperator(Operator.GREATER);
        System.out.println("node::" + an2.toPPString());
        assertNotNull(an2.toPPString());
    }
}
