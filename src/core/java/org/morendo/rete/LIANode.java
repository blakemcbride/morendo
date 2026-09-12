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
import org.morendo.rete.exception.RetractException;

import java.util.Iterator;

/**
 * @author Peter Lin
 *     <p>LIANode stands for Left Input Adapter Node. Left input adapter node can only only have 1
 *     alphaNode above it. Left input adapater nodes are not shared by multiple branches of the
 *     network, so it doesn't have any memory.
 */
public class LIANode extends BaseAlpha {

    /** The node this adapter hangs off; its alpha memory holds the facts that reach us. */
    private BaseAlpha parent = null;

    /** */
    public LIANode(int id) {
        super(id);
    }

    public void setParent(BaseAlpha parent) {
        this.parent = parent;
    }

    public BaseAlpha getParent() {
        return this.parent;
    }

    /** the implementation just propogates the assert down the network */
    public void assertFact(Fact fact, Rete engine, WorkingMemory mem) throws AssertException {
        propogateAssert(fact, engine, mem);
    }

    /**
     * Propogate the assert to the successor nodes
     *
     * @param fact
     * @param engine
     */
    protected void propogateAssert(Fact fact, Rete engine, WorkingMemory mem)
            throws AssertException {
        for (int idx = 0; idx < this.successorNodes.length; idx++) {
            BaseNode nNode = this.successorNodes[idx];
            if (nNode instanceof BaseJoin next) {
                Fact[] newf = {fact};
                next.assertLeft(new Index(newf), engine, mem);
            } else if (nNode instanceof TerminalNode terminalNode) {
                Fact[] newf = {fact};
                TerminalNode tn = terminalNode;
                tn.assertFacts(new Index(newf), engine, mem);
            }
        }
    }

    /** Retract simply propogates it down the network */
    public void retractFact(Fact fact, Rete engine, WorkingMemory mem) throws RetractException {
        propogateRetract(fact, engine, mem);
    }

    /**
     * propogate the retract
     *
     * @param fact
     * @param engine
     */
    protected void propogateRetract(Fact fact, Rete engine, WorkingMemory mem)
            throws RetractException {
        for (int idx = 0; idx < this.successorNodes.length; idx++) {
            BaseNode nNode = this.successorNodes[idx];
            if (nNode instanceof BaseJoin next) {
                Fact[] newf = {fact};
                next.retractLeft(new Index(newf), engine, mem);
            } else if (nNode instanceof TerminalNode next) {
                Fact[] newf = {fact};
                next.retractFacts(new Index(newf), engine, mem);
            }
        }
    }

    /**
     * Set the next node in the sequence of 1-input nodes. The next node can be an AlphaNode or a
     * LIANode.
     *
     * @param node
     */
    public void addSuccessorNode(BaseNode node, Rete engine, WorkingMemory mem)
            throws AssertException {
        if (addNode(node)) {
            // the adapter keeps no memory: replay the facts held by the node it hangs off,
            // so that a rule defined after the facts still sees them
            AlphaMemory alpha = mem.getAlphaMemory(this.parent != null ? this.parent : this);
            if (alpha.size() > 0) {
                Iterator<?> itr = alpha.iterator();
                while (itr.hasNext()) {
                    Fact fact = (Fact) itr.next();
                    if (node instanceof BaseAlpha next) {
                        next.assertFact(fact, engine, mem);
                    } else if (node instanceof BaseJoin next) {
                        next.assertLeft(new Index(new Fact[] {fact}), engine, mem);
                    } else if (node instanceof TerminalNode next) {
                        next.assertFacts(new Index(new Fact[] {fact}), engine, mem);
                    }
                }
            }
        }
    }

    public String hashString() {
        return toString();
    }

    /** the Left Input Adapter Node returns zero length string */
    public String toString() {
        return "";
    }

    /** the Left input Adapter Node returns zero length string */
    public String toPPString() {
        return "";
    }
}
