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

/**
 * Hangs off the last join of a forall sub-network and hands its tuples to the right side of a
 * {@link TupleNotJoin}. It has no memory and no successors of its own.
 */
public class RightInputAdapter extends BaseJoin {

    private final TupleNotJoin target;

    public RightInputAdapter(int id, TupleNotJoin target) {
        super(id);
        this.target = target;
    }

    public void assertLeft(Index linx, Rete engine, WorkingMemory mem) throws AssertException {
        this.target.assertRightTuple(linx, engine, mem);
    }

    public void retractLeft(Index linx, Rete engine, WorkingMemory mem) throws RetractException {
        this.target.retractRightTuple(linx, engine, mem);
    }

    public void assertRight(Fact rfact, Rete engine, WorkingMemory mem) throws AssertException {}

    public void retractRight(Fact rfact, Rete engine, WorkingMemory mem) throws RetractException {}

    public void clear(WorkingMemory mem) {}

    public String toString() {
        return this.target.getLabel() + " input of node " + this.target.getNodeId();
    }

    public String toPPString() {
        return toString();
    }
}
