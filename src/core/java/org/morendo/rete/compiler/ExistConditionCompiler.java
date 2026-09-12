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
package org.morendo.rete.compiler;

import org.morendo.rete.BaseAlpha;
import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.Binding;
import org.morendo.rete.DefaultQueryCompiler;
import org.morendo.rete.DefaultRuleCompiler;
import org.morendo.rete.ExistJoin;
import org.morendo.rete.ExistJoinFrst;
import org.morendo.rete.ExistNeqJoin;
import org.morendo.rete.ExistPredJoin;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.ObjectTypeNode;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.query.QueryBaseJoin;
import org.morendo.rete.query.QueryExistFrst;
import org.morendo.rete.query.QueryExistFuncJoin;
import org.morendo.rete.query.QueryExistJoin;
import org.morendo.rete.query.QueryExistNeqJoin;
import org.morendo.rete.query.QueryObjTypeNode;
import org.morendo.rule.Condition;
import org.morendo.rule.ExistCondition;
import org.morendo.rule.GraphQuery;
import org.morendo.rule.ObjectCondition;
import org.morendo.rule.Query;
import org.morendo.rule.Rule;

/**
 * @author HouZhanbin
 * @author Peter Lin Oct 12, 2007 9:50:17 AM
 */
public class ExistConditionCompiler extends AbstractConditionCompiler {

    private ConditionCompiler conditionCompiler;

    public ExistConditionCompiler(ConditionCompiler conditionCompiler) {
        this.conditionCompiler = conditionCompiler;
        this.ruleCompiler = (DefaultRuleCompiler) conditionCompiler.getRuleCompiler();
        this.queryCompiler = (DefaultQueryCompiler) conditionCompiler.getQueryCompiler();
    }

    public ExistConditionCompiler(GraphQueryCompiler compiler) {
        this.graphCompiler = compiler;
    }

    /** Method will compile exists quantifier */
    public void compile(Condition condition, int position, Rule rule, boolean alphaMemory) {
        ExistCondition cond = (ExistCondition) condition;
        ObjectCondition oc = (ObjectCondition) cond;
        conditionCompiler.compile(oc, position, rule, alphaMemory);
    }

    public void compile(Condition condition, int position, Query query) {
        ExistCondition cond = (ExistCondition) condition;
        ObjectCondition oc = (ObjectCondition) cond;
        conditionCompiler.compile(oc, position, query);
    }

    /**
     * TODO - note the logic feels a bit messy. Need to rethink it and make it simpler. When the
     * first conditional element is Exist, it can only have literal constraints, so we shouldn't
     * need to check if the last node is a join. That doesn't make any sense. Need to rethink this
     * and clean it up. Peter Lin 10/14/2007
     */
    public void compileFirstJoin(Condition condition, Rule rule) throws AssertException {
        BaseJoin bjoin = new ExistJoinFrst(ruleCompiler.getEngine().nextNodeId());
        ExistCondition cond = (ExistCondition) condition;
        BaseNode base = cond.getLastNode();
        if (base != null) {
            if (base instanceof BaseAlpha baseAlpha) {
                ruleCompiler.attachJoinNode(baseAlpha, bjoin);
            } else if (base instanceof BaseJoin baseJoin) {
                ruleCompiler.attachJoinNode(baseJoin, bjoin);
            }
        } else {
            // the rule doesn't have a literal constraint so we need to add
            // ExistJoinFrst as a child
            ObjectTypeNode otn = ruleCompiler.findObjectTypeNode(cond.getTemplateName());
            ruleCompiler.attachJoinNode(otn, bjoin);
        }
        // important, do not call this before ExistJoinFrst is added
        // if it's called first, the arraylist will return index
        // out of bound, since there's nothing in the list
        cond.addNode(bjoin);
    }

    public void compileFirstJoin(Condition condition, Query query) throws AssertException {
        BaseJoin bjoin = new ExistJoinFrst(queryCompiler.getEngine().nextNodeId());
        ExistCondition cond = (ExistCondition) condition;
        BaseNode base = cond.getLastNode();
        if (base != null) {
            if (base instanceof BaseAlpha baseAlphaValue) {
                (baseAlphaValue).addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
            } else if (base instanceof BaseJoin baseJoinValue) {
                (baseJoinValue).addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
            }
        } else {
            // the rule doesn't have a literal constraint so we need to add
            // ExistJoinFrst as a child
            QueryObjTypeNode otn = queryCompiler.findObjectTypeNode(cond.getTemplateName());
            otn.addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
        }
        // important, do not call this before ExistJoinFrst is added
        // if it's called first, the arraylist will return index
        // out of bound, since there's nothing in the list
        cond.addNode(bjoin);
    }

    /**
     * @param condition
     * @param query
     * @throws AssertException
     */
    public void compileFirstJoin(Condition condition, GraphQuery query) throws AssertException {
        BaseJoin bjoin = new ExistJoinFrst(graphCompiler.getEngine().nextNodeId());
        ExistCondition cond = (ExistCondition) condition;
        BaseNode base = cond.getLastNode();
        if (base != null) {
            if (base instanceof BaseAlpha) {
                ((BaseAlpha) base).addSuccessorNode(bjoin, graphCompiler.getEngine(), null);
            } else if (base instanceof BaseJoin) {
                ((BaseJoin) base).addSuccessorNode(bjoin, graphCompiler.getEngine(), null);
            }
        } else {
            QueryObjTypeNode otn = queryCompiler.findObjectTypeNode(cond.getTemplateName());
            otn.addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
        }
        cond.addNode(bjoin);
    }

    /**
     * method compiles ExistCE to an exist node. It does not include rules that start with Exist for
     * the first CE.
     */
    public BaseJoin compileJoin(
            Condition condition, int position, Rule rule, Condition previousCond) {
        ExistCondition exc = (ExistCondition) condition;
        Binding[] binds = getBindings(exc, rule, position);
        BaseJoin joinNode = null;
        if (exc.isHasPredicateJoin()) {
            joinNode = new ExistPredJoin(ruleCompiler.getEngine().nextNodeId());
        } else if (exc.isHasNotEqual()) {
            joinNode = new ExistNeqJoin(ruleCompiler.getEngine().nextNodeId());
        } else {
            joinNode = new ExistJoin(ruleCompiler.getEngine().nextNodeId());
        }
        joinNode.setBindings(binds);
        return joinNode;
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, Query query, Condition previousCond) {
        ExistCondition exc = (ExistCondition) condition;
        Binding[] binds = getBindings(exc, query, position);
        QueryBaseJoin joinNode = null;
        if (exc.isHasPredicateJoin()) {
            joinNode = new QueryExistFuncJoin(queryCompiler.getEngine().nextNodeId());
        } else if (exc.isHasNotEqual()) {
            joinNode = new QueryExistNeqJoin(queryCompiler.getEngine().nextNodeId());
        } else {
            joinNode = new QueryExistJoin(queryCompiler.getEngine().nextNodeId());
        }
        joinNode.setBindings(binds);
        return joinNode;
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, GraphQuery query, Condition previousCond) {
        ExistCondition exc = (ExistCondition) condition;
        Binding[] binds = getBindings(exc, query, position);
        QueryBaseJoin joinNode = null;
        if (exc.isHasPredicateJoin()) {
            joinNode = new QueryExistFuncJoin(queryCompiler.getEngine().nextNodeId());
        } else if (exc.isHasNotEqual()) {
            joinNode = new QueryExistNeqJoin(queryCompiler.getEngine().nextNodeId());
        } else {
            joinNode = new QueryExistJoin(queryCompiler.getEngine().nextNodeId());
        }
        joinNode.setBindings(binds);
        return joinNode;
    }

    @Override
    ObjectCondition getObjectCondition(Condition condition) {
        return (ObjectCondition) condition;
    }

    public void compileSingleCE(Rule rule) throws AssertException {
        Condition[] conds = rule.getConditions();
        Condition condition = conds[0];
        ExistCondition cond = (ExistCondition) condition;
        BaseNode base = cond.getLastNode();
        BaseJoin bjoin = new ExistJoinFrst(ruleCompiler.getEngine().nextNodeId());
        if (base != null) {
            if (base instanceof BaseAlpha) {
                ruleCompiler.attachJoinNode(base, bjoin);
            } else if (base instanceof BaseJoin) {
                ruleCompiler.attachJoinNode(base, bjoin);
            }
        } else {
            // the rule doesn't have a literal constraint so we need to add
            // ExistJoinFrst as a child
            ObjectTypeNode otn = ruleCompiler.findObjectTypeNode(cond.getTemplateName());
            ruleCompiler.attachJoinNode(otn, bjoin);
        }
        // important, do not call this before ExistJoinFrst is added
        // if it's called first, the arraylist will return index
        // out of bound, since there's nothing in the list
        cond.addNode(bjoin);
        rule.addJoinNode(bjoin);
    }

    public void compileSingleCE(Query query) throws AssertException {
        Condition[] conds = query.getConditions();
        Condition condition = conds[0];
        ExistCondition cond = (ExistCondition) condition;
        BaseNode base = cond.getLastNode();
        QueryBaseJoin bjoin = new QueryExistFrst(queryCompiler.getEngine().nextNodeId());
        if (base != null) {
            if (base instanceof BaseAlpha) {
                ((BaseAlpha) base).addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
            } else if (base instanceof BaseJoin) {
                ((BaseJoin) base).addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
            }
        } else {
            // the rule doesn't have a literal constraint so we need to add
            // ExistJoinFrst as a child
            QueryObjTypeNode otn = queryCompiler.findObjectTypeNode(cond.getTemplateName());
            otn.addSuccessorNode(bjoin, queryCompiler.getEngine(), null);
        }
        // important, do not call this before ExistJoinFrst is added
        // if it's called first, the arraylist will return index
        // out of bound, since there's nothing in the list
        cond.addNode(bjoin);
        query.addJoinNode(bjoin);
    }
}
