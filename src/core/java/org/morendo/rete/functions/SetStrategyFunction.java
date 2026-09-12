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
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.Strategy;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.strategies.Strategies;

/**
 * @author Peter Lin
 */
public class SetStrategyFunction implements Function {

    /** */
    public static final String SET_STRATEGY = "set-strategy";

    public SetStrategyFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        String strategy = null;
        String old = engine.getCurrentFocus().getStrategy().getName();
        if (params != null && params.length == 1) {
            strategy = params[0].getStringValue();
            Strategy strat = Strategies.getStrategy(strategy);
            if (strat != null) {
                engine.getWorkingMemory().setStrategy(strat);
            }
            strategy = engine.getCurrentFocus().getStrategy().getName();
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.STRING, old);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return SET_STRATEGY;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(set-strategy depth|breadth)";
    }
}
