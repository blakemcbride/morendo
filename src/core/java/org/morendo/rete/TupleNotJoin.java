/*
 * Copyright 2026 Blake McBride
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.Arrays;
import java.util.Map;

/**
 * A not-join whose right input is a stream of tuples rather than facts: the output of a sub-network
 * that starts from this node's own left input. A left tuple passes while no right tuple begins with
 * it. This is what {@code forall} compiles to: {@code (forall A B)} is "no A without B", a
 * TupleNotJoin whose right side is the tuples (left, A) that found no B.
 */
public class TupleNotJoin extends BaseJoin {

    /** The number of facts in a left tuple; right tuples repeat them as a prefix. */
    private final int prefixWidth;

    public TupleNotJoin(int id, int prefixWidth) {
        super(id);
        this.prefixWidth = prefixWidth;
    }

    private Index prefix(Index right) {
        Fact[] facts = right.getFacts();
        return facts.length == this.prefixWidth
                ? right
                : new Index(Arrays.copyOf(facts, this.prefixWidth));
    }

    public void assertLeft(Index linx, Rete engine, WorkingMemory mem) throws AssertException {
        Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
        Map<Index, int[]> rightmem = mem.getBetaRightMemory(this);
        int[] blockers = rightmem.get(linx);
        int[] count = new int[] {blockers == null ? 0 : blockers[0]};
        leftmem.put(linx, count);
        if (count[0] == 0) {
            propagateAssert(linx, engine, mem);
        }
    }

    public void retractLeft(Index linx, Rete engine, WorkingMemory mem) throws RetractException {
        Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
        int[] count = leftmem.remove(linx);
        if (count != null && count[0] == 0) {
            propagateRetract(linx, engine, mem);
        }
    }

    /** A tuple of the sub-network arrived: the left tuple it extends is blocked. */
    public void assertRightTuple(Index rinx, Rete engine, WorkingMemory mem)
            throws AssertException {
        Index key = prefix(rinx);
        Map<Index, int[]> rightmem = mem.getBetaRightMemory(this);
        int[] blockers = rightmem.get(key);
        if (blockers == null) {
            blockers = new int[1];
            rightmem.put(key, blockers);
        }
        blockers[0]++;
        Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
        int[] count = leftmem.get(key);
        if (count != null && ++count[0] == 1) {
            try {
                propagateRetract(key, engine, mem);
            } catch (RetractException e) {
                throw new AssertException("forall - " + e.getMessage());
            }
        }
    }

    /** A tuple of the sub-network went away: the left tuple passes again when none is left. */
    public void retractRightTuple(Index rinx, Rete engine, WorkingMemory mem)
            throws RetractException {
        Index key = prefix(rinx);
        Map<Index, int[]> rightmem = mem.getBetaRightMemory(this);
        int[] blockers = rightmem.get(key);
        if (blockers == null) {
            return;
        }
        if (--blockers[0] <= 0) {
            rightmem.remove(key);
        }
        Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
        int[] count = leftmem.get(key);
        if (count != null && --count[0] == 0) {
            try {
                propagateAssert(key, engine, mem);
            } catch (AssertException e) {
                throw new RetractException("forall - " + e.getMessage());
            }
        }
    }

    /**
     * The right input is tuples, delivered through {@link RightInputAdapter}; facts never arrive
     * here.
     */
    public void assertRight(Fact rfact, Rete engine, WorkingMemory mem) throws AssertException {}

    public void retractRight(Fact rfact, Rete engine, WorkingMemory mem) throws RetractException {}

    /** Replays the tuples that pass into a new successor. */
    public void addSuccessorNode(BaseJoin node, Rete engine, WorkingMemory mem)
            throws AssertException {
        if (addNode(node)) {
            Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
            for (Map.Entry<Index, int[]> entry : leftmem.entrySet()) {
                if (entry.getValue()[0] == 0) {
                    node.assertLeft(entry.getKey(), engine, mem);
                }
            }
        }
    }

    public void addSuccessorNode(TerminalNode node, Rete engine, WorkingMemory mem)
            throws AssertException {
        if (addNode(node)) {
            Map<Index, int[]> leftmem = mem.getBetaLeftMemory(this);
            for (Map.Entry<Index, int[]> entry : leftmem.entrySet()) {
                if (entry.getValue()[0] == 0) {
                    node.assertFacts(entry.getKey(), engine, mem);
                }
            }
        }
    }

    public void clear(WorkingMemory mem) {
        Map<?, ?> leftmem = mem.getBetaLeftMemory(this);
        leftmem.clear();
        Map<?, ?> rightmem = mem.getBetaRightMemory(this);
        rightmem.clear();
    }

    public String toString() {
        return "forall (" + this.prefixWidth + " outer facts)";
    }

    public String toPPString() {
        return "forall node " + this.nodeID;
    }
}
