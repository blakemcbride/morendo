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
import org.morendo.rete.util.NodeUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Peter Lin
 *     <p>MultipleNeqJoin is a special case for exists join when there's multiple matches. It uses
 *     NotEqualHashIndex for conditional elements that have constraints that use not equal to.
 */
public class MultipleNeqJoin extends BaseJoin {

    /** */
    public MultipleNeqJoin(int id) {
        super(id);
    }

    /** clear will clear the lists */
    public void clear(WorkingMemory mem) {
        Map<?, ?> rightmem = mem.getBetaRightMemory(this);
        Map<?, ?> leftmem = mem.getBetaRightMemory(this);
        Iterator<?> itr = leftmem.keySet().iterator();
        // first we iterate over the list for each fact
        // and clear it.
        while (itr.hasNext()) {
            BetaMemory bmem = (BetaMemory) leftmem.get(itr.next());
            bmem.clear();
        }
        // now that we've cleared the list for each fact, we
        // can clear the Map.
        leftmem.clear();
        rightmem.clear();
    }

    /**
     * assertLeft takes an array of facts. Since the next join may be joining against one or more
     * objects, we need to pass all previously matched facts.
     *
     * @param factInstance
     * @param engine
     */
    public void assertLeft(Index linx, Rete engine, WorkingMemory mem) throws AssertException {
        Map<Index, Index> leftmem = mem.getBetaLeftMemory(this);
        leftmem.put(linx, linx);
        NotEqHashIndex inx =
                new NotEqHashIndex(NodeUtils.getLeftBindValues(this.binds, linx.getFacts()));
        HashedNeqAlphaMemory rightmem = mem.getBetaRightMemory(this);
        Object[] objs = rightmem.iterator(inx);
        // if the right side has 1 match, we propogate the original
        // index down the network. We don't add any facts to the index
        if (objs != null && objs.length > 1) {
            this.propagateAssert(linx, engine, mem);
        }
    }

    /**
     * Assert from the right side is always going to be from an Alpha node.
     *
     * @param factInstance
     * @param engine
     */
    public void assertRight(Fact rfact, Rete engine, WorkingMemory mem) throws AssertException {
        HashedNeqAlphaMemory rightmem = mem.getBetaRightMemory(this);
        NotEqHashIndex inx = new NotEqHashIndex(NodeUtils.getRightBindValues(this.binds, rfact));

        Map<?, ?> leftmem = mem.getBetaLeftMemory(this);
        Iterator<?> itr = leftmem.values().iterator();
        while (itr.hasNext()) {
            Index linx = (Index) itr.next();
            // propagate the left tuple when rfact is its second match, i.e. the
            // tuple's own bind values had exactly one match before rfact was
            // added (rfact's bind values say nothing about that)
            if (this.evaluate(linx.getFacts(), rfact)
                    && rightmem.matchCount(leftIndex(linx)) == 1) {
                this.propagateAssert(linx, engine, mem);
            }
        }
        rightmem.addPartialMatch(inx, rfact, engine);
    }

    /**
     * Retracting from the left is different than retractRight for couple of reasons.
     *
     * <ul>
     *   <li>NotJoin will only propogate the facts from the left
     *   <li>NotJoin never needs to merge the left and right
     * </ul>
     *
     * @param factInstance
     * @param engine
     */
    public void retractLeft(Index linx, Rete engine, WorkingMemory mem) throws RetractException {
        Map<?, ?> leftmem = mem.getBetaLeftMemory(this);
        leftmem.remove(linx);
        propagateRetract(linx, engine, mem);
    }

    /**
     * Retract from the right works in the following order. 1. remove the fact from the right memory
     * 2. check which left memory matched 3. propogate the retract
     *
     * @param factInstance
     * @param engine
     */
    public void retractRight(Fact rfact, Rete engine, WorkingMemory mem) throws RetractException {
        NotEqHashIndex inx = new NotEqHashIndex(NodeUtils.getRightBindValues(this.binds, rfact));
        HashedNeqAlphaMemory rightmem = mem.getBetaRightMemory(this);
        // first we remove the fact from the right
        rightmem.removePartialMatch(inx, rfact);
        Map<?, ?> leftmem = mem.getBetaLeftMemory(this);
        Iterator<?> itr = leftmem.values().iterator();
        while (itr.hasNext()) {
            Index linx = (Index) itr.next();
            // retract the left tuple when rfact was one of its two matches,
            // i.e. exactly one is left
            if (this.evaluate(linx.getFacts(), rfact)
                    && rightmem.matchCount(leftIndex(linx)) == 1) {
                propagateRetract(linx, engine, mem);
            }
        }
    }

    /** The index of a left tuple's bind values, for looking up its matches in the right memory. */
    private NotEqHashIndex leftIndex(Index linx) {
        return new NotEqHashIndex(NodeUtils.getLeftBindValues(this.binds, linx.getFacts()));
    }

    /**
     * Method will use the right binding to perform the evaluation of the join. Since we are
     * building joins similar to how CLIPS and other rule engines handle it, it means 95% of the
     * time the right fact list only has 1 fact.
     *
     * @param leftlist
     * @param right
     * @return
     */
    public boolean evaluate(Fact[] leftlist, Fact right) {
        boolean eval = true;
        // we iterate over the binds and evaluate the facts
        for (int idx = 0; idx < this.binds.length; idx++) {
            Binding bnd = binds[idx];
            eval = bnd.evaluate(leftlist, right);
            if (!eval) {
                break;
            }
        }
        return eval;
    }

    /**
     * simple implementation for toString. may need to change the format later so it looks nicer.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("OnlyNeqJoin - ");
        for (int idx = 0; idx < this.binds.length; idx++) {
            if (idx > 0) {
                buf.append(" && ");
            }
            buf.append(this.binds[idx].toBindString());
        }
        return buf.toString();
    }

    /** The current implementation is similar to BetaNode */
    public String toPPString() {
        StringBuilder buf = new StringBuilder();
        buf.append("<node-" + this.nodeID + "> OnlyNeqJoin - ");
        if (binds != null && binds.length > 0) {
            for (int idx = 0; idx < this.binds.length; idx++) {
                if (idx > 0) {
                    buf.append(" && ");
                }
                buf.append(this.binds[idx].toPPString());
            }
        } else {
            buf.append(" no joins ");
        }
        return buf.toString();
    }
}
