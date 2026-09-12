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

import org.jamocha.rete.Constants;
import org.jamocha.rete.Rete;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Lin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReteInitTest {



    @Test
    public void testInit() {
        Rete engine = new Rete();
        assertNotNull(engine);
    }
    
    @Test
    public void testInitModule() {
        Rete engine = new Rete();
        assertNotNull(engine);
        assertNotNull(engine.getCurrentFocus());
        assertNotNull(engine.getCurrentFocus().getModuleName());
        assertEquals(engine.getCurrentFocus().getModuleName(),Constants.MAIN_MODULE);
        System.out.println("default module is " + engine.getCurrentFocus().getModuleName());
    }
    
    /**
     * Simple test to make sure the nodeId method work correctly
     *
     */
    @Test
    public void testNodeId() {
        Rete engine = new Rete();
        assertNotNull(engine);
        // A fresh engine has already allocated node ids for its built-in templates
        // (initial fact plus the Graph, Node and Edge templates), so test relative to
        // whatever the first peek returns.
        int first = engine.peakNextNodeId();
        assertEquals(first,engine.peakNextNodeId());
        assertEquals(first,engine.peakNextNodeId());
        assertEquals(first,engine.nextNodeId());
        assertEquals(first + 1,engine.nextNodeId());
        assertEquals(first + 2,engine.nextNodeId());
        int id = engine.nextNodeId();
        assertEquals(first + 3,id);
        System.out.println("first free node id on a fresh engine is " + first + ", last allocated is " + id);
    }
}
