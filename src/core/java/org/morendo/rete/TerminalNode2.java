/*
 * Copyright 2002-2008 Peter Lin
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
package org.morendo.rete;

import org.morendo.rete.exception.AssertException;
import org.morendo.rule.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Lin
 *     <p>TerminalNode2 is different than TerminalNode in that it uses a different Activation
 *     implementation. Rather than use BasicActivation, it uses LinkedActivation.
 */
public class TerminalNode2 extends TerminalNode {

    /** */

    /**
     * @param id
     */
    public TerminalNode2(int id, Rule rl) {
        super(id, rl);
        this.theRule = rl;
    }

    /** The terminal nodes doesn't have a memory, so the method does nothing. */
    public void clear(WorkingMemory mem) {
        Map<?, ?> tmem = (Map<?, ?>) mem.getTerminalMemory(this);
        if (tmem != null) {
            tmem.clear();
        }
    }

    /**
     * @param facts
     * @param engine
     */
    public void assertFacts(Index inx, Rete engine, WorkingMemory mem) {
        if (expired(inx, engine)) {
            return;
        }
        activate(inx, engine, mem);
    }

    /**
     * Creates the activation for a match, records it in the terminal memory and adds it to the
     * agenda.
     */
    protected LinkedActivation activate(Index inx, Rete engine, WorkingMemory mem) {
        LinkedActivation act = new LinkedActivation(this.theRule, inx);
        act.setTerminalNode(this);
        Map<Index, Activation> tmem = mem.getTerminalMemory(this);
        tmem.put(inx, act);
        // add the activation to the current module's activation list.
        engine.getAgenda().addActivation(act);
        return act;
    }

    /**
     * The match is gone: forget it, and take its activation off the agenda if it has not fired.
     *
     * @param facts
     * @param engine
     */
    public void retractFacts(Index inx, Rete engine, WorkingMemory mem) {
        Map<?, ?> tmem = (Map<?, ?>) mem.getTerminalMemory(this);
        LinkedActivation act = (LinkedActivation) tmem.remove(inx);
        if (act != null && !act.isFired()) {
            engine.getAgenda().removeActivation(act);
        }
    }

    /**
     * Puts the rule back on the agenda for every match that has already fired, the CLIPS refresh
     * command, and returns how many activations were added.
     */
    public int refresh(Rete engine, WorkingMemory mem) {
        Map<Index, Activation> tmem = mem.getTerminalMemory(this);
        List<Index> fired = new ArrayList<>();
        for (Map.Entry<Index, Activation> entry : tmem.entrySet()) {
            if (entry.getValue() instanceof LinkedActivation act && act.isFired()) {
                fired.add(entry.getKey());
            }
        }
        for (Index inx : fired) {
            activate(inx, engine, mem);
        }
        return fired.size();
    }

    /**
     * Return the Rule object associated with this terminal node
     *
     * @return
     */
    public Rule getRule() {
        return this.theRule;
    }

    /**
     * Called when the activation fires. The match stays in the terminal memory, marked as fired, so
     * that {@link #refresh} can activate it again; it leaves when its facts are retracted.
     *
     * @param LinkedActivation
     */
    public void removeActivation(WorkingMemory mem, LinkedActivation activation) {
        activation.setFired(true);
    }

    /** method does not apply to termial nodes. therefore it's not implemented */
    public void addSuccessorNode(BaseNode node, Rete engine, WorkingMemory mem)
            throws AssertException {}

    /** return the name of the rule */
    public String toString() {
        return this.theRule.getName();
    }

    /** return the name of the rule */
    public String toPPString() {
        return this.theRule.getName();
    }
}
