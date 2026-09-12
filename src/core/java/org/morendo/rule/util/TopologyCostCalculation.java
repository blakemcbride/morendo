/*
 * Copyright 2002-2009 Jamocha
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
package org.morendo.rule.util;

import org.morendo.rete.BaseAlpha;
import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.HashedNotEqBNode;
import org.morendo.rete.HashedNotEqNJoin;
import org.morendo.rete.LIANode;
import org.morendo.rete.ObjectTypeNode;
import org.morendo.rete.Rete;
import org.morendo.rete.RootNode;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.Template;
import org.morendo.rule.*;

import java.util.Iterator;
import java.util.List;

public class TopologyCostCalculation {

    public static final int STANDARD = 1000;
    public static final int OPTIMIZED = 2000;

    private int MODE = OPTIMIZED;

    public TopologyCostCalculation() {
        super();
    }

    public int getMode() {
        return this.MODE;
    }

    public void setMode(int m) {
        this.MODE = m;
    }

    public void calculateCost(Rete engine, Defrule r, RootNode root) {
        if (this.MODE == STANDARD) {
            this.calculateWithoutOptimization(engine, r, root);
        } else {
            this.calculateWithOptimization(engine, r, root);
        }
    }

    public void calculateWithoutOptimization(Rete engine, Defrule r, RootNode root) {
        Condition[] conditions = r.getConditions();
        // first we add the size for the object type nodes
        int cost = 0;
        RuleCompiler compiler = engine.getRuleCompiler();
        Template template = null;
        // the first step is to add the number of successors for the object type nodes
        for (int idx = 0; idx < conditions.length; idx++) {
            if (conditions[idx] instanceof ObjectCondition) {
                ObjectCondition oc = (ObjectCondition) conditions[idx];
                template = oc.getTemplate();
                // add 1 for object type node
                cost++;
                ObjectTypeNode otn = compiler.getObjectTypeNode(template);
                cost = cost + otn.getStandardCostValue();
            }
        }
        // now iterate over the alpha nodes and their children
        for (int idx = 0; idx < conditions.length; idx++) {
            if (conditions[idx] instanceof ObjectCondition) {
                ObjectCondition oc = (ObjectCondition) conditions[idx];
                List<?> nodes = oc.getNodes();
                if (nodes.size() > 1) {
                    for (int idz = 1; idz < nodes.size(); idz++) {
                        BaseNode node = (BaseNode) nodes.get(idz);
                        if (node instanceof BaseAlpha && !(node instanceof LIANode)) {
                            cost++;
                        }
                    }
                }
            }
        }
        List<?> joins = r.getJoins();
        Iterator<?> joinItr = joins.iterator();
        while (joinItr.hasNext()) {
            BaseJoin join = (BaseJoin) joinItr.next();
            cost = cost + 4;
            if (join instanceof HashedNotEqBNode || join instanceof HashedNotEqNJoin) {
                cost++;
            }
        }

        // set the cost value
        r.setCostValue(cost);
    }

    public void calculateWithOptimization(Rete engine, Defrule r, RootNode root) {
        Condition[] conditions = r.getConditions();
        // first we add the size for the object type nodes
        int cost = 0;
        RuleCompiler compiler = engine.getRuleCompiler();
        Template template = null;
        // the first step is to add the number of successors for the object type nodes
        for (int idx = 0; idx < conditions.length; idx++) {
            if (conditions[idx] instanceof ObjectCondition) {
                ObjectCondition oc = (ObjectCondition) conditions[idx];
                template = oc.getTemplate();
                // add 1 for object type node
                cost++;
                ObjectTypeNode otn = compiler.getObjectTypeNode(template);
                cost = cost + otn.getOptimizedCostValue();
            }
        }
        // now iterate over the alpha nodes and their children
        for (int idx = 0; idx < conditions.length; idx++) {
            if (conditions[idx] instanceof ObjectCondition) {
                ObjectCondition oc = (ObjectCondition) conditions[idx];
                List<?> nodes = oc.getNodes();
                if (nodes.size() > 1) {
                    for (int idz = 1; idz < nodes.size(); idz++) {
                        BaseNode node = (BaseNode) nodes.get(idz);
                        if (node instanceof BaseAlpha && !(node instanceof LIANode)) {
                            cost++;
                        }
                    }
                }
            }
        }
        List<?> joins = r.getJoins();
        Iterator<?> joinItr = joins.iterator();
        while (joinItr.hasNext()) {
            BaseJoin join = (BaseJoin) joinItr.next();
            cost = cost + 4;
            if (join instanceof HashedNotEqBNode || join instanceof HashedNotEqNJoin) {
                cost++;
            }
        }

        // set the cost value
        r.setCostValue(cost);
    }
}
