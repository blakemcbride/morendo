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
import org.morendo.rule.ForallCondition;
import org.morendo.rule.ObjectCondition;
import org.morendo.rule.Query;
import org.morendo.rule.Rule;

/**
 * Compiles {@code (forall A B+)} as "no A without the Bs". With L the tuples entering the forall:
 * the sub-network joins L with A, then with every B; a first TupleNotJoin over (L, A) blocks the
 * (L, A) tuples that reach the end of the B chain, so it emits the A matches that lack a B; a
 * second TupleNotJoin over L blocks the L tuples those extend. Its output, L unchanged, is what the
 * rest of the rule sees.
 */
public class ForallConditionCompiler implements ConditionCompiler {

    DefaultRuleCompiler ruleCompiler;
    DefaultQueryCompiler queryCompiler;
    GraphQueryCompiler graphCompiler;

    private final ConditionCompiler patterns;

    public ForallConditionCompiler(ConditionCompiler objectConditionCompiler) {
        this.patterns = objectConditionCompiler;
    }

    public RuleCompiler getRuleCompiler() {
        return this.ruleCompiler;
    }

    public QueryCompiler getQueryCompiler() {
        return this.queryCompiler;
    }

    /** Compiles the alpha side of every pattern; A takes the next row, the Bs the rows after. */
    public void compile(Condition condition, int position, Rule rule, boolean alphaMemory) {
        ForallCondition forall = (ForallCondition) condition;
        forall.setOuterWidth(position);
        this.patterns.compile(forall.getFirst(), position, rule, alphaMemory);
        int row = position + 1;
        for (ObjectCondition oc : forall.getRest()) {
            this.patterns.compile(oc, row++, rule, alphaMemory);
        }
    }

    public void compile(Condition condition, int position, Query query) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    /** A forall that opens the rule hangs off the initial fact. */
    public void compileFirstJoin(Condition condition, Rule rule) throws AssertException {
        ForallCondition forall = (ForallCondition) condition;
        build(forall, rule);
        connect(forall, initialFactNode());
    }

    public void compileFirstJoin(Condition condition, Query query) throws AssertException {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    /** Builds the sub-network and returns the outer node; the left source is attached later. */
    public BaseJoin compileJoin(
            Condition condition, int position, Rule rule, Condition previousCond) {
        ForallCondition forall = (ForallCondition) condition;
        try {
            build(forall, rule);
        } catch (AssertException e) {
            throw new IllegalStateException(e);
        }
        return forall.getOuter();
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, Query query, Condition previousCond) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            BaseJoin previousJoinNode,
            BaseJoin joinNode)
            throws AssertException {
        BaseNode left =
                previousJoinNode != null ? previousJoinNode : previousCondition.getLastNode();
        connect((ForallCondition) condition, left);
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            QueryBaseJoin previousJoinNode,
            QueryBaseJoin joinNode) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    public void connectJoinNode(
            Condition previousCondition,
            Condition condition,
            QueryBaseJoin previousJoinNode,
            QueryBaseJoin joinNode,
            GraphQueryCompiler compiler) {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    /** A rule whose only condition is a forall: the sub-network hangs off the initial fact. */
    public void compileSingleCE(Rule rule) throws AssertException {
        ForallCondition forall = (ForallCondition) rule.getConditions()[0];
        build(forall, rule);
        connect(forall, initialFactNode());
    }

    public void compileSingleCE(Query query) throws AssertException {
        throw new UnsupportedOperationException("forall is supported in rules only");
    }

    private LIANode initialFactNode() {
        ObjectTypeNode otn =
                ruleCompiler.getInputnodes().get(ruleCompiler.getEngine().getInitFact());
        return ruleCompiler.findLIANode(otn);
    }

    /** Creates the nodes and wires everything but the left source. */
    private void build(ForallCondition forall, Rule rule) throws AssertException {
        int width = forall.getOuterWidth();
        ObjectCondition first = forall.getFirst();
        // L joined with A
        BaseJoin joinA = this.patterns.compileJoin(first, width, rule, null);
        attachPattern(first, joinA);
        // ... then with every B
        BaseJoin last = joinA;
        int row = width + 1;
        for (ObjectCondition oc : forall.getRest()) {
            BaseJoin joinB = this.patterns.compileJoin(oc, row++, rule, null);
            ruleCompiler.attachJoinNode(last, joinB);
            attachPattern(oc, joinB);
            last = joinB;
        }
        // the (L, A) tuples with no complete B chain
        TupleNotJoin missing = new TupleNotJoin(ruleCompiler.getEngine().nextNodeId(), width + 1);
        ruleCompiler.attachJoinNode(joinA, missing);
        ruleCompiler.attachJoinNode(
                last, new RightInputAdapter(ruleCompiler.getEngine().nextNodeId(), missing));
        // the L tuples with no such A
        TupleNotJoin outer = new TupleNotJoin(ruleCompiler.getEngine().nextNodeId(), width);
        ruleCompiler.attachJoinNode(
                missing, new RightInputAdapter(ruleCompiler.getEngine().nextNodeId(), outer));
        forall.setNodes(joinA, outer);
    }

    /** The tuples entering the forall feed both the A join and the outer node. */
    private void connect(ForallCondition forall, BaseNode left) throws AssertException {
        ruleCompiler.attachJoinNode(left, forall.getEntry());
        ruleCompiler.attachJoinNode(left, forall.getOuter());
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
