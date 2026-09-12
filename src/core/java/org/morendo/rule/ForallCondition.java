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
package org.morendo.rule;

import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.TupleNotJoin;
import org.morendo.rete.compiler.CompilerProvider;
import org.morendo.rete.compiler.ConditionCompiler;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code (forall A B+)}: for every fact matching A there are facts matching every B. Compiled as
 * "no A without the Bs": a {@link org.morendo.rete.TupleNotJoin} over a sub-network. Rules only;
 * the patterns are plain ones, and a variable first bound inside the forall is local to it.
 */
public final class ForallCondition implements Condition {

    private final ObjectCondition first;
    private final List<ObjectCondition> rest = new ArrayList<>();
    private final List<BaseNode> nodes = new ArrayList<>();

    /** The number of facts in the tuples entering the forall, set when compiled. */
    private int outerWidth = 0;

    private BaseJoin entry = null;
    private TupleNotJoin outer = null;

    public ForallCondition(ObjectCondition first, List<ObjectCondition> rest) {
        this.first = first;
        this.rest.addAll(rest);
    }

    public ObjectCondition getFirst() {
        return this.first;
    }

    public List<ObjectCondition> getRest() {
        return this.rest;
    }

    public int getOuterWidth() {
        return this.outerWidth;
    }

    public void setOuterWidth(int width) {
        this.outerWidth = width;
    }

    /**
     * The join of the outer tuples with the first pattern, and the node the rule continues from.
     */
    public void setNodes(BaseJoin entry, TupleNotJoin outer) {
        this.entry = entry;
        this.outer = outer;
        this.nodes.clear();
        this.nodes.add(outer);
    }

    public BaseJoin getEntry() {
        return this.entry;
    }

    public TupleNotJoin getOuter() {
        return this.outer;
    }

    public boolean compare(Condition cond) {
        return cond instanceof ForallCondition other
                && this.first.compare(other.first)
                && this.rest.size() == other.rest.size();
    }

    public List<?> getNodes() {
        return this.nodes;
    }

    public void addNode(BaseNode node) {
        this.nodes.add(node);
    }

    public void addNewAlphaNodes(BaseNode node) {}

    public BaseNode getLastNode() {
        return this.nodes.isEmpty() ? null : this.nodes.get(this.nodes.size() - 1);
    }

    public void clear() {
        this.nodes.clear();
        this.first.clear();
        for (ObjectCondition oc : this.rest) {
            oc.clear();
        }
    }

    public String toPPString() {
        StringBuilder buf = new StringBuilder("(forall ");
        buf.append(this.first.toPPString().trim());
        for (ObjectCondition oc : this.rest) {
            buf.append(' ').append(oc.toPPString().trim());
        }
        return buf.append(')').toString();
    }

    public ConditionCompiler getCompiler(RuleCompiler ruleCompiler) {
        return CompilerProvider.getInstance(ruleCompiler).forallConditionCompiler;
    }

    public ConditionCompiler getCompiler(QueryCompiler ruleCompiler) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    public ConditionCompiler getCompiler(GraphQueryCompiler ruleCompiler) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    public List<Object> getBindConstraints() {
        return new ArrayList<>();
    }
}
