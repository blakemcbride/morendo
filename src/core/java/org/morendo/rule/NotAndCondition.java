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
import org.morendo.rete.Binding;
import org.morendo.rete.Constants;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.TupleNotJoin;
import org.morendo.rete.compiler.CompilerProvider;
import org.morendo.rete.compiler.ConditionCompiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code (not (and CE+))}: no combination of facts matches every element of the group. The elements
 * are patterns, negated patterns, tests and nested groups. Compiled as a {@link TupleNotJoin} over
 * a sub-network that joins the tuples entering the group with its elements. Rules only; a variable
 * first bound inside the group is local to it.
 */
public final class NotAndCondition implements Condition {

    private final List<Condition> conditions = new ArrayList<>();
    private final List<BaseNode> nodes = new ArrayList<>();
    private final List<BaseJoin> entries = new ArrayList<>();

    /** The bindings of the variables first bound inside the group, kept off the rule. */
    private Map<String, Binding> localBindings = new LinkedHashMap<>();

    /** The number of facts in the tuples entering the group, set when compiled. */
    private int outerWidth = 0;

    private TupleNotJoin outer = null;

    public NotAndCondition(List<? extends Condition> conditions) {
        this.conditions.addAll(conditions);
    }

    public Condition[] getConditions() {
        return this.conditions.toArray(new Condition[0]);
    }

    public int getOuterWidth() {
        return this.outerWidth;
    }

    public void setOuterWidth(int width) {
        this.outerWidth = width;
    }

    public Map<String, Binding> getLocalBindings() {
        return this.localBindings;
    }

    public void setLocalBindings(Map<String, Binding> bindings) {
        this.localBindings = bindings;
    }

    /** The nodes that take the tuples entering the group, and the node the rule continues from. */
    public void setNodes(List<BaseJoin> entries, TupleNotJoin outer) {
        this.entries.clear();
        this.entries.addAll(entries);
        this.outer = outer;
        this.nodes.clear();
        this.nodes.add(outer);
    }

    public List<BaseJoin> getEntries() {
        return this.entries;
    }

    public TupleNotJoin getOuter() {
        return this.outer;
    }

    public boolean compare(Condition cond) {
        if (!(cond instanceof NotAndCondition other)
                || this.conditions.size() != other.conditions.size()) {
            return false;
        }
        for (int i = 0; i < this.conditions.size(); i++) {
            if (!this.conditions.get(i).compare(other.conditions.get(i))) {
                return false;
            }
        }
        return true;
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
        this.entries.clear();
        for (Condition c : this.conditions) {
            c.clear();
        }
    }

    public String toPPString() {
        StringBuilder buf = new StringBuilder("  (not (and" + Constants.LINEBREAK);
        for (Condition c : this.conditions) {
            for (String line : c.toPPString().split("\\R")) {
                if (!line.isBlank()) {
                    buf.append("  ").append(line).append(Constants.LINEBREAK);
                }
            }
        }
        return buf.append("  ))").append(Constants.LINEBREAK).toString();
    }

    public ConditionCompiler getCompiler(RuleCompiler ruleCompiler) {
        return CompilerProvider.getInstance(ruleCompiler).notAndConditionCompiler;
    }

    public ConditionCompiler getCompiler(QueryCompiler ruleCompiler) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    public ConditionCompiler getCompiler(GraphQueryCompiler ruleCompiler) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    public List<Object> getBindConstraints() {
        return new ArrayList<>();
    }
}
