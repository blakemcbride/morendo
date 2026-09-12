/*
 * Copyright 2002-2010 Peter Lin
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

import org.morendo.rete.BaseNode;
import org.morendo.rete.Index;
import org.morendo.rete.Rete;
import org.morendo.rete.WorkingMemory;
import org.morendo.rete.exception.AssertException;
import org.morendo.rule.Defquery;

import java.util.ArrayList;
import java.util.List;

public class QueryResultNode extends BaseNode {

    /** */
    protected ArrayList<Object> results = new ArrayList<>();

    private volatile Defquery query = null;

    public QueryResultNode(int id) {
        super(id);
    }

    public Defquery getQuery() {
        return query;
    }

    public void setQuery(Defquery query) {
        this.query = query;
    }

    public void addResult(Index facts, Rete engine, WorkingMemory mem) {
        results.add(facts.getFacts());
    }

    public List<?> getResults() {
        return this.results;
    }

    /** Forgets the results of the previous run. */
    public void clear() {
        this.results.clear();
    }

    /**
     * Method is not implemented, since QueryResultNode is the end of the discrimination network.
     */
    public void addSuccessorNode(BaseNode node, Rete engine, WorkingMemory mem)
            throws AssertException {}

    /** Method is not implemented since the node has no children. */
    public void removeAllSuccessors() {}

    public String toPPString() {
        return this.query.toPPString();
    }

    public String toString() {
        return this.query.toString();
    }

    public QueryResultNode clone(Rete engine, Defquery query) {
        QueryResultNode clone = new QueryResultNode(engine.nextNodeId());
        clone.query = query;
        query.setQueryResultNode(clone);
        return clone;
    }
}
