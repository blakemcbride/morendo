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
package org.morendo.rete.query;

import org.morendo.rete.BetaMemory;
import org.morendo.rete.Binding;
import org.morendo.rete.EqHashIndex;
import org.morendo.rete.Fact;
import org.morendo.rete.HashedAlphaMemoryImpl;
import org.morendo.rete.Index;
import org.morendo.rete.Rete;
import org.morendo.rete.WorkingMemory;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.util.NodeUtils;
import org.morendo.rule.Defquery;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Peter Lin
 *     <p>MultipleJoin is a specialized version of exists join that is satified if only multiple
 *     matches are found. By multiple, we mean more than 1.
 */
public class QueryMultipleJoin extends QueryBaseJoin {

    /** */
    public QueryMultipleJoin(int id) {
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
        EqHashIndex inx = new EqHashIndex(NodeUtils.getLeftValues(this.binds, linx.getFacts()));
        HashedAlphaMemoryImpl rightmem = mem.getBetaRightMemory(this);
        if (rightmem.count(inx) > 1) {
            this.propogateAssert(linx, engine, mem);
        }
    }

    /**
     * Assert from the right side is always going to be from an Alpha node.
     *
     * @param factInstance
     * @param engine
     */
    public void assertRight(Fact rfact, Rete engine, WorkingMemory mem) throws AssertException {
        HashedAlphaMemoryImpl rightmem = mem.getBetaRightMemory(this);
        EqHashIndex inx = new EqHashIndex(NodeUtils.getRightValues(this.binds, rfact));
        int after = rightmem.addPartialMatch(inx, rfact, engine);
        Map<?, ?> leftmem = mem.getBetaLeftMemory(this);
        Iterator<?> itr = leftmem.values().iterator();
        while (itr.hasNext()) {
            Index linx = (Index) itr.next();
            if (this.evaluate(linx.getFacts(), rfact)) {
                if (after > 1) {
                    this.propogateAssert(linx, engine, mem);
                }
            }
        }
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
        buf.append("Only - ");
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
        buf.append("<node-" + this.nodeID + "> Only - ");
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

    @Override
    public QueryBaseJoin clone(Rete engine, Defquery query) {
        // TODO Auto-generated method stub
        return null;
    }
}
