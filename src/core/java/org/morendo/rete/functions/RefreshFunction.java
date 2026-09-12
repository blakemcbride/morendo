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
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.Rule;

import java.math.BigDecimal;

/**
 * {@code (refresh <rule>)}: puts the rule back on the agenda for every match that has already
 * fired; returns how many activations were added.
 */
public class RefreshFunction implements Function {

    public static final String REFRESH = "refresh";

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        java.util.List<Rule> rules =
                params != null && params.length == 1
                        ? engine.getCurrentFocus().findRules(params[0].getStringValue())
                        : java.util.List.of();
        if (rules.isEmpty()) {
            ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE));
        } else {
            int added = 0;
            for (Rule rule : rules) {
                added += engine.refreshRule(rule);
            }
            ret.addReturnValue(
                    new DefaultReturnValue(ValueType.BIG_DECIMAL, BigDecimal.valueOf(added)));
        }
        return ret;
    }

    public String getName() {
        return REFRESH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(refresh <rule>)\n re-activates the rule for the matches that already fired;"
                + " returns the number of activations added.";
    }
}
