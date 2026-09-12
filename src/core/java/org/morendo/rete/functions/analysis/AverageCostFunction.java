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

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.Defrule;

import java.util.Collection;
import java.util.Iterator;

public class AverageCostFunction implements Function {

    /** */
    public static final String AVERAGE_COST = "average-cost";

    public AverageCostFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        int total = 0;
        Collection<?> rules = engine.getCurrentFocus().getAllRules();
        Iterator<?> ruleItr = rules.iterator();
        while (ruleItr.hasNext()) {
            Defrule rule = (Defrule) ruleItr.next();
            total += rule.getCostValue();
        }
        int average = total / rules.size();
        DefaultReturnValue v =
                new DefaultReturnValue(ValueType.INTEGER_OBJECT, Integer.valueOf(average));
        ret.addReturnValue(v);
        return ret;
    }

    public String getName() {
        return AVERAGE_COST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(average-cost)";
    }
}
