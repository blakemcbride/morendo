package org.morendo.rete.query;

import org.morendo.rete.Rete;
import org.morendo.rete.WorkingMemory;
import org.morendo.rete.exception.AssertException;

public abstract class QueryBaseNot extends QueryBaseJoin {

    /** */
    public QueryBaseNot(int nodeId) {
        super(nodeId);
    }

    public abstract void executeJoin(Rete engine, WorkingMemory mem) throws AssertException;
}
