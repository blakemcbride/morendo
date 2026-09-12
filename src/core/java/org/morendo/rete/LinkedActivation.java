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

import org.morendo.rete.exception.ExecuteException;
import org.morendo.rule.Action;
import org.morendo.rule.Rule;

/**
 * @author Peter Lin
 *     <p>LinkedActivation is different than BasicActivation in a couple of ways. LinkedActivation
 *     makes it easier to remove Activations from an ActivationList, without having to iterate over
 *     the activations. When the activation is executed or removed, it needs to make sure it checks
 *     the previous and next and set them correctly.
 */
public class LinkedActivation implements Activation {

    /** */
    private LinkedActivation prev = null;

    private LinkedActivation next = null;

    private Rule theRule;

    private Index index;

    private long aggreTime = -1;

    private TerminalNode2 tnode = null;

    /** Set when the activation has fired; the terminal node keeps it as a record of the match. */
    private boolean fired = false;

    /** */
    public LinkedActivation(Rule rule, Index inx) {
        super();
        this.theRule = rule;
        this.index = inx;
        // calculateTime(inx.getFacts());
    }

    /**
     * The sum of the fact ids. Ids grow with assertion order, so a larger sum is a more recent
     * match; unlike the wall clock it is the same from one run to the next, which keeps the agenda
     * order deterministic.
     */
    protected void calculateTime(Fact[] facts) {
        long sum = 0;
        for (int idx = 0; idx < facts.length; idx++) {
            sum += facts[idx].getFactId();
        }
        this.aggreTime = sum;
    }

    public boolean isFired() {
        return this.fired;
    }

    public void setFired(boolean fired) {
        this.fired = fired;
    }

    public long getAggregateTime() {
        if (this.aggreTime == -1) {
            this.calculateTime(this.index.getFacts());
        }
        return this.aggreTime;
    }

    public Fact[] getFacts() {
        return this.index.getFacts();
    }

    public Index getIndex() {
        return this.index;
    }

    public Rule getRule() {
        return this.theRule;
    }

    /**
     * the method will set the previous activation to the one passed to the method and it will also
     * set previous.next to this instance.
     *
     * @param previous
     */
    public void setPrevious(LinkedActivation previous) {
        this.prev = previous;
        if (previous != null) {
            previous.next = this;
        }
    }

    public LinkedActivation getPrevious() {
        return this.prev;
    }

    /**
     * the method will set the next activation to the one passed to the method and it will set
     * next.prev to this instance.
     *
     * @param next
     */
    public void setNext(LinkedActivation next) {
        this.next = next;
        if (next != null) {
            next.prev = this;
        }
    }

    public LinkedActivation getNext() {
        return this.next;
    }

    public void setTerminalNode(TerminalNode2 node) {
        this.tnode = node;
    }

    public TerminalNode2 getTerminalNode() {
        return this.tnode;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Activation#compare(woolfel.engine.rete.Activation)
     */
    public boolean compare(Activation act) {
        if (act == this) {
            return true;
        }
        if (act.getRule() == this.theRule && act.getIndex().equals(this.index)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove the Activation from the list and set the previous and next activation correctly.
     * There's basically 3 cases we have to handle. 1. first 2. last 3. somewhere in between The
     * current implementation will first set the previous and next. Once they are correctly set, it
     * will set the references to those LinkedActivation to null.
     */
    public void remove() {
        if (this.prev != null && this.next != null) {
            this.prev.setNext(this.next);
        } else if (this.prev != null && this.next == null) {
            this.prev.setNext(null);
        } else if (this.prev == null && this.next != null) {
            this.next.setPrevious(null);
        }
        this.prev = null;
        this.next = null;
    }

    /**
     * method is used to make sure the activation is removed from TerminalNode2.
     *
     * @param engine
     */
    protected void remove(Rete engine) {
        if (tnode != null) {
            tnode.removeActivation(engine.getWorkingMemory(), this);
        }
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Activation#executeActivation(woolfel.engine.rete.Rete)
     */
    public void executeActivation(Rete engine) throws ExecuteException {
        // if previous and next are not null, set the previous/next
        // of each and then set the reference to null
        remove(engine);
        try {
            this.theRule.setTriggerFacts(this.index.getFacts());
            Action[] actions = this.theRule.getActions();
            for (int idx = 0; idx < actions.length; idx++) {
                if (actions[idx] != null) {
                    actions[idx].executeAction(engine, this.index.getFacts());
                } else {
                    throw new ExecuteException(ExecuteException.NULL_ACTION);
                }
            }
        } catch (org.morendo.rete.functions.control.ControlFlow flow) {
            // (return) ends the actions of this firing; (break) outside a loop does the same
        } catch (ExecuteException e) {
            throw e;
        }
    }

    public void clear() {
        this.theRule = null;
        this.tnode = null;
    }

    public String toPPString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Activation: " + this.theRule.getName());
        Fact[] facts = this.index.getFacts();
        for (int idx = 0; idx < facts.length; idx++) {
            buf.append(", id-" + facts[idx].getFactId());
        }
        buf.append(" AggrTime-" + this.aggreTime);
        return buf.toString();
    }

    public LinkedActivation clone() {
        LinkedActivation la = new LinkedActivation(this.theRule, this.index);
        return la;
    }
}
