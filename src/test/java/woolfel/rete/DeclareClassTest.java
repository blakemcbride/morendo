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
import org.morendo.rete.BaseSlot;
import org.morendo.rete.Deftemplate;
import org.morendo.rete.Rete;
import org.morendo.rete.Template;

import woolfel.examples.model.Account;
import woolfel.examples.model.Account2;
import woolfel.examples.model.Account3;
import woolfel.examples.model.BackupAccount;
import woolfel.examples.model.TestBean3;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Peter Lin
 *     <p>Test the declareObject functionality
 */
public class DeclareClassTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testDeclareClass() {
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 1, count);
        System.out.println("number of Defclass is " + count);
        Collection templates = engine.getCurrentFocus().getTemplates();
        assertEquals(baseTemplates + 1, templates.size());
        Iterator itr = templates.iterator();
        while (itr.hasNext()) {
            Deftemplate dtemp = (Deftemplate) itr.next();
            System.out.println(dtemp.toPPString());
        }
        System.out.println("--------------------------------");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testDeclareClass2() {
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        engine.declareObject(TestBean3.class);
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 2, count);
        System.out.println("number of Defclass is " + count);
        Collection templates = engine.getCurrentFocus().getTemplates();
        assertEquals(baseTemplates + 2, templates.size());
        Iterator itr = templates.iterator();
        while (itr.hasNext()) {
            Deftemplate dtemp = (Deftemplate) itr.next();
            System.out.println(dtemp.toPPString());
        }
        System.out.println("--------------------------------");
    }

    @Test
    public void testDeftemplate() {
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        assertNotNull(engine.getCurrentFocus().getTemplates());
        int count = engine.getCurrentFocus().getTemplateCount();
        assertEquals(baseTemplates + 1, count);
        System.out.println("number of Deftemplates is " + count);
    }

    @Test
    public void testDeclareClassInheritance() {
        System.out.println("\ntestDeclareClassInheritance");
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        engine.declareObject(BackupAccount.class);
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 2, count);
        System.out.println("number of Defclass is " + count);
        Template acctemp = engine.getCurrentFocus().getTemplate(Account.class.getName());
        Template bkacc = engine.getCurrentFocus().getTemplate(BackupAccount.class.getName());
        BaseSlot[] accslots = acctemp.getAllSlots();
        BaseSlot[] bkslots = bkacc.getAllSlots();
        for (int idx = 0; idx < accslots.length; idx++) {
            assertTrue(accslots[idx].getName().equals(bkslots[idx].getName()));
            System.out.println(accslots[idx].getName() + "=" + bkslots[idx].getName());
        }
    }

    @Test
    public void testDeclareClassInheritance2() {
        System.out.println("\ntestDeclareClassInheritance2");
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(BackupAccount.class);
        engine.declareObject(Account.class);
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 2, count);
        System.out.println("number of Defclass is " + count);
        Template acctemp = engine.getCurrentFocus().getTemplate(Account.class.getName());
        Template bkacc = engine.getCurrentFocus().getTemplate(BackupAccount.class.getName());
        BaseSlot[] accslots = acctemp.getAllSlots();
        BaseSlot[] bkslots = bkacc.getAllSlots();
        for (int idx = 0; idx < accslots.length; idx++) {
            assertTrue(accslots[idx].getName().equals(bkslots[idx].getName()));
            System.out.println(accslots[idx].getName() + "=" + bkslots[idx].getName());
        }
    }

    @Test
    public void testDeclareClassInheritance3() {
        System.out.println("\ntestDeclareClassInheritance3");
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        engine.declareObject(Account2.class, null, Account.class.getName());
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 2, count);
        System.out.println("number of Defclass is " + count);
        Template acctemp = engine.getCurrentFocus().getTemplate(Account.class.getName());
        Template acc2 = engine.getCurrentFocus().getTemplate(Account2.class.getName());
        BaseSlot[] accslots = acctemp.getAllSlots();
        BaseSlot[] acc2slots = acc2.getAllSlots();
        for (int idx = 0; idx < accslots.length; idx++) {
            assertTrue(accslots[idx].getName().equals(acc2slots[idx].getName()));
            System.out.println(accslots[idx].getName() + "=" + acc2slots[idx].getName());
        }
    }

    @Test
    public void testDeclareClassInheritance4() {
        System.out.println("\ntestDeclareClassInheritance3");
        Rete engine = new Rete();
        assertNotNull(engine);
        int baseClasses = engine.getDefclasses().size();
        int baseTemplates = engine.getCurrentFocus().getTemplateCount();
        engine.declareObject(Account.class);
        engine.declareObject(Account2.class, null, Account.class.getName());
        engine.declareObject(Account3.class, null, Account2.class.getName());
        int count = engine.getDefclasses().size();
        assertEquals(baseClasses + 3, count);
        System.out.println("number of Defclass is " + count);
        Template acctemp = engine.getCurrentFocus().getTemplate(Account.class.getName());
        Template acc3 = engine.getCurrentFocus().getTemplate(Account3.class.getName());
        BaseSlot[] accslots = acctemp.getAllSlots();
        BaseSlot[] acc3slots = acc3.getAllSlots();
        for (int idx = 0; idx < accslots.length; idx++) {
            assertTrue(accslots[idx].getName().equals(acc3slots[idx].getName()));
            System.out.println(accslots[idx].getName() + "=" + acc3slots[idx].getName());
        }
    }
}
