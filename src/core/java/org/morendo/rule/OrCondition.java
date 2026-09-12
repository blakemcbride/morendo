/*
 * Copyright 2002-2006 Peter Lin
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
package org.morendo.rule;

import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.compiler.ConditionCompiler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Lin
 *     <p>AndCondition is specifically created to handle and conjunctions. AndConditions are
 *     compiled to a BetaNode.
 */
public final class OrCondition implements Condition {

    /** */
    protected List<Object> nestedCE = new ArrayList<>();

    protected BaseJoin reteNode = null;

    /** */
    public OrCondition() {
        super();
    }

    public boolean compare(Condition cond) {
        if (!(cond instanceof OrCondition)) {
            return false;
        }
        OrCondition orc = (OrCondition) cond;
        if (orc.getNestedConditionalElement().size() == this.nestedCE.size()) {
            return true;
        } else {
            return false;
        }
    }

    public void addNestedConditionElement(Object ce) {
        this.nestedCE.add(ce);
    }

    public List<Object> getNestedConditionalElement() {
        return this.nestedCE;
    }

    public List<?> getNodes() {
        return new ArrayList<>();
    }

    /** not implemented yet */
    public void addNode(BaseNode node) {}

    /** not implemented yet */
    public void addNewAlphaNodes(BaseNode node) {}

    public BaseNode getLastNode() {
        return reteNode;
    }

    public void clear() {
        reteNode = null;
    }

    public String toPPString() {
        return "";
    }

    public ConditionCompiler getCompiler(RuleCompiler ruleCompiler) {
        // TODO Auto-generated method stub
        return null;
    }

    public ConditionCompiler getCompiler(QueryCompiler ruleCompiler) {
        // TODO Auto-generated method stub
        return null;
    }

    public ConditionCompiler getCompiler(GraphQueryCompiler ruleCompiler) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Object> getBindConstraints() {
        // TODO Auto-generated method stub
        return null;
    }
}
