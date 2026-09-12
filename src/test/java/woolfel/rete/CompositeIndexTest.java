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
import org.morendo.rete.CompositeIndex;
import org.morendo.rete.Defclass;
import org.morendo.rete.Deftemplate;
import org.morendo.rete.Fact;
import org.morendo.rete.Operator;

import woolfel.examples.model.TestBean2;

import java.util.HashMap;

/**
 * @author Peter Lin
 *     <p>Simple testcase for the CompositeIndex used by ObjectTypeNode
 */
public class CompositeIndexTest {

    @Test
    public void testEqual() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("testString");
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr1", Operator.EQUAL, fact.getSlotValue(0));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
    }

    @Test
    public void testNotEqual() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("testString");
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr1", Operator.NOTEQUAL, fact.getSlotValue(0));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
    }

    @Test
    public void testNil() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr1", Operator.NILL, fact.getSlotValue(0));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
    }

    @Test
    public void testNotNil() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("testString");
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr1", Operator.NOTNILL, fact.getSlotValue(0));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIndex() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("testString");
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr1", Operator.EQUAL, fact.getSlotValue(0));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
        HashMap map = new HashMap();
        map.put(ci, bean);

        CompositeIndex ci2 = new CompositeIndex("attr1", Operator.EQUAL, fact.getSlotValue(0));
        assertTrue(map.containsKey(ci2));

        CompositeIndex ci3 = new CompositeIndex("attr1", Operator.NOTEQUAL, fact.getSlotValue(0));
        assertFalse(map.containsKey(ci3));

        CompositeIndex ci4 = new CompositeIndex("attr1", Operator.NILL, fact.getSlotValue(0));
        assertFalse(map.containsKey(ci4));

        CompositeIndex ci5 = new CompositeIndex("attr1", Operator.NOTNILL, fact.getSlotValue(0));
        assertFalse(map.containsKey(ci5));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIndex2() {
        Defclass dc = new Defclass(TestBean2.class);
        Deftemplate dtemp = (Deftemplate) dc.createDeftemplate("testBean2");
        TestBean2 bean = new TestBean2();
        bean.setAttr1("testString");
        bean.setAttr2(1);
        short a3 = 3;
        bean.setAttr3(a3);
        long a4 = 101;
        bean.setAttr4(a4);
        float a5 = 10101;
        bean.setAttr5(a5);
        double a6 = 101.101;
        bean.setAttr6(a6);

        Fact fact = dtemp.createFact(bean, dc, 1);
        assertNotNull(fact);
        System.out.println(fact.toFactString());
        CompositeIndex ci = new CompositeIndex("attr2", Operator.EQUAL, fact.getSlotValue(1));
        assertNotNull(ci);
        System.out.println(ci.toPPString());
        HashMap map = new HashMap();
        map.put(ci, bean);

        CompositeIndex ci2 = new CompositeIndex("attr2", Operator.EQUAL, fact.getSlotValue(1));
        assertTrue(map.containsKey(ci2));

        CompositeIndex ci3 = new CompositeIndex("attr2", Operator.NOTEQUAL, fact.getSlotValue(1));
        assertFalse(map.containsKey(ci3));

        CompositeIndex ci4 = new CompositeIndex("attr2", Operator.NILL, fact.getSlotValue(1));
        assertFalse(map.containsKey(ci4));

        CompositeIndex ci5 = new CompositeIndex("attr2", Operator.NOTNILL, fact.getSlotValue(1));
        assertFalse(map.containsKey(ci5));
    }
}
