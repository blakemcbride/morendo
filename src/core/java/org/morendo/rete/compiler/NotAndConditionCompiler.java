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
package org.morendo.rete.compiler;

import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.Binding;
import org.morendo.rete.DefaultQueryCompiler;
import org.morendo.rete.DefaultRuleCompiler;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.LIANode;
import org.morendo.rete.ObjectTypeNode;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RightInputAdapter;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.TupleNotJoin;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.query.QueryBaseJoin;
import org.morendo.rule.Condition;
import org.morendo.rule.NotAndCondition;
import org.morendo.rule.ObjectCondition;
import org.morendo.rule.Query;
import org.morendo.rule.Rule;
import org.morendo.rule.TestCondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles {@code (not (and CE+))}. With L the tuples entering the group, the sub-network joins L
 * with the group's elements in order: a pattern through its join, a negated pattern through its
 * not-join, a test through its test node, and a nested group through its own sub-network. A
 * TupleNotJoin over L blocks every L tuple that a tuple at the end of that chain extends; its
 * output, L unchanged, is what the rest of the rule sees.
 *
 * <p>Each positive pattern of the group takes the next row after L. A variable first bound inside
 * the group is local to it: its binding is taken off the rule once the alpha side is compiled, so
 * that a later pattern or action of the rule does not see it, and put back only while the group's
 * joins are built.
 */
public class NotAndConditionCompiler implements ConditionCompiler {

    DefaultRuleCompiler ruleCompiler;
    DefaultQueryCompiler queryCompiler;
    GraphQueryCompiler graphCompiler;

    private final ConditionCompiler patterns;
    private final ConditionCompiler tests;

    public NotAndConditionCompiler(
            ConditionCompiler objectConditionCompiler, ConditionCompiler testConditionCompiler) {
        this.patterns = objectConditionCompiler;
        this.tests = testConditionCompiler;
    }

    public RuleCompiler getRuleCompiler() {
        return this.ruleCompiler;
    }

    public QueryCompiler getQueryCompiler() {
        return this.queryCompiler;
    }

    /** Compiles the alpha side of every element and keeps the group's own variables local. */
    public void compile(Condition condition, int position, Rule rule, boolean alphaMemory) {
        NotAndCondition group = (NotAndCondition) condition;
        group.setOuterWidth(position);
        Set<String> before = bindingNames(rule);
        int row = position;
        for (Condition element : group.getConditions()) {
            element.getCompiler(this.ruleCompiler).compile(element, row, rule, alphaMemory);
            if (takesRow(element)) {
                row++;
            }
        }
        Map<String, Binding> local = new LinkedHashMap<>();
        for (String name : bindingNames(rule)) {
            if (!before.contains(name)) {
                Binding binding = rule.removeBinding(name);
                if (binding != null) {
                    local.put(name, binding);
                }
            }
        }
        group.setLocalBindings(local);
    }

    public void compile(Condition condition, int position, Query query) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    /** A group that opens the rule hangs off the initial fact. */
    public void compileFirstJoin(Condition condition, Rule rule) throws AssertException {
        NotAndCondition group = (NotAndCondition) condition;
        build(group, rule);
        connect(group, initialFactNode());
    }

    public void compileFirstJoin(Condition condition, Query query) throws AssertException {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    /** Builds the sub-network and returns the outer node; the left source is attached later. */
    public BaseJoin compileJoin(
            Condition condition, int position, Rule rule, Condition previousCond) {
        NotAndCondition group = (NotAndCondition) condition;
        try {
            build(group, rule);
        } catch (AssertException e) {
            throw new IllegalStateException(e);
        }
        return group.getOuter();
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, Query query, Condition previousCond) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            BaseJoin previousJoinNode,
            BaseJoin joinNode)
            throws AssertException {
        BaseNode left =
                previousJoinNode != null ? previousJoinNode : previousCondition.getLastNode();
        connect((NotAndCondition) condition, left);
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            QueryBaseJoin previousJoinNode,
            QueryBaseJoin joinNode) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            QueryBaseJoin previousJoinNode,
            QueryBaseJoin joinNode,
            GraphQueryCompiler compiler) {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    /** A rule whose only condition is a group: the sub-network hangs off the initial fact. */
    public void compileSingleCE(Rule rule) throws AssertException {
        NotAndCondition group = (NotAndCondition) rule.getConditions()[0];
        build(group, rule);
        connect(group, initialFactNode());
    }

    public void compileSingleCE(Query query) throws AssertException {
        throw new UnsupportedOperationException("(not (and ...)) is supported in rules only");
    }

    /** A positive, plain pattern adds a fact to the tuples; nothing else in a group does. */
    private static boolean takesRow(Condition element) {
        return element.getClass() == ObjectCondition.class
                && !((ObjectCondition) element).getNegated();
    }

    private static Set<String> bindingNames(Rule rule) {
        Set<String> names = new HashSet<>();
        for (Iterator<?> itr = rule.getBindingIterator(); itr.hasNext(); ) {
            names.add(((Binding) itr.next()).getVarName());
        }
        return names;
    }

    private LIANode initialFactNode() {
        ObjectTypeNode otn =
                ruleCompiler.getInputnodes().get(ruleCompiler.getEngine().getInitFact());
        return ruleCompiler.findLIANode(otn);
    }

    /** Creates the group's nodes and wires everything but the left source. */
    private void build(NotAndCondition group, Rule rule) throws AssertException {
        Map<String, Binding> displaced = showLocalBindings(group, rule);
        try {
            int row = group.getOuterWidth();
            List<BaseJoin> entries = new ArrayList<>();
            BaseJoin last = null;
            for (Condition element : group.getConditions()) {
                // the nodes that take the previous element's tuples, and the node giving this
                // element's tuples to the next
                List<BaseJoin> inputs = new ArrayList<>();
                BaseJoin output;
                if (element instanceof NotAndCondition nested) {
                    build(nested, rule);
                    inputs.addAll(nested.getEntries());
                    inputs.add(nested.getOuter());
                    output = nested.getOuter();
                } else if (element instanceof TestCondition) {
                    output = this.tests.compileJoin(element, row, rule, null);
                    inputs.add(output);
                } else {
                    ObjectCondition pattern = (ObjectCondition) element;
                    output = this.patterns.compileJoin(pattern, row, rule, null);
                    attachPattern(pattern, output);
                    inputs.add(output);
                }
                if (last == null) {
                    entries.addAll(inputs);
                } else {
                    for (BaseJoin input : inputs) {
                        ruleCompiler.attachJoinNode(last, input);
                    }
                }
                last = output;
                if (takesRow(element)) {
                    row++;
                }
            }
            TupleNotJoin outer =
                    new TupleNotJoin(
                            ruleCompiler.getEngine().nextNodeId(),
                            group.getOuterWidth(),
                            "not/and");
            ruleCompiler.attachJoinNode(
                    last, new RightInputAdapter(ruleCompiler.getEngine().nextNodeId(), outer));
            group.setNodes(entries, outer);
        } finally {
            hideLocalBindings(rule, displaced);
        }
    }

    /**
     * Puts the group's local bindings on the rule for building its joins, and returns the bindings
     * of the same names they displace (null where there was none).
     */
    private static Map<String, Binding> showLocalBindings(NotAndCondition group, Rule rule) {
        Map<String, Binding> displaced = new LinkedHashMap<>();
        for (Map.Entry<String, Binding> local : group.getLocalBindings().entrySet()) {
            displaced.put(local.getKey(), rule.removeBinding(local.getKey()));
            rule.addBinding(local.getKey(), local.getValue());
        }
        return displaced;
    }

    /** Takes the local bindings off the rule again and restores the ones they displaced. */
    private static void hideLocalBindings(Rule rule, Map<String, Binding> displaced) {
        for (Map.Entry<String, Binding> entry : displaced.entrySet()) {
            rule.removeBinding(entry.getKey());
            if (entry.getValue() != null) {
                rule.addBinding(entry.getKey(), entry.getValue());
            }
        }
    }

    /** The tuples entering the group feed its first element and the outer node. */
    private void connect(NotAndCondition group, BaseNode left) throws AssertException {
        for (BaseJoin entry : group.getEntries()) {
            ruleCompiler.attachJoinNode(left, entry);
        }
        ruleCompiler.attachJoinNode(left, group.getOuter());
    }

    /** The alpha side of a pattern feeds the join's right input. */
    private void attachPattern(ObjectCondition oc, BaseJoin join) throws AssertException {
        if (oc.getNodes().size() > 0) {
            ruleCompiler.attachJoinNode(oc.getLastNode(), join);
        } else {
            ruleCompiler.attachJoinNode(
                    ruleCompiler.findObjectTypeNode(oc.getTemplateName()), join);
        }
    }
}
