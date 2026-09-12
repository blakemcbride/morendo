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
package org.morendo.rete.functions.analysis;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rule.Defrule;
import org.morendo.rule.util.TopologyCostCalculation;

/**
 * @author Peter Lin
 */
public class TopologyCostFunction implements Function {

    /** */
    public static final String TOPOLOGY_COST = "topology-cost";

    TopologyCostCalculation costFunction = new TopologyCostCalculation();

    /** */
    public TopologyCostFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        if (params != null && params.length > 0) {
            String ruleName = null;
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof ValueParam) {
                    ValueParam n = (ValueParam) params[idx];
                    ruleName = n.getStringValue();
                } else if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    ruleName = (String) bp.getValue(engine, ValueType.STRING);
                }
                Defrule r = (Defrule) engine.getCurrentFocus().findRule(ruleName);
                if (r != null) {
                    costFunction.calculateCost(engine, r, engine.getRootNode());
                }
            }
        }
        return ret;
    }

    public String getName() {
        return TOPOLOGY_COST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(topology-cost <literal>+)\n"
                + "Function description:\n"
                + "\tCalculates the topology cost of one or more rules and sets the"
                + "\n\tRule.costValue.";
    }
}
