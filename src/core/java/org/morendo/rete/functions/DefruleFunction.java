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
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rule.Defrule;

/**
 * @author Peter Lin
 */
public class DefruleFunction implements Function {

    /** */
    public static final String DEFRULE = "defrule";

    public DefruleFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean add = Boolean.TRUE;
        if (params.length == 1 && params[0].getValue() instanceof Defrule[] group) {
            // a rule with an or group: one rule per combination of alternatives
            for (Defrule rl : group) {
                if (!engine.getCurrentFocus().containsRule(rl)) {
                    add = engine.getRuleCompiler().addRule(rl) && add;
                }
            }
        } else if (params.length == 1 && params[0].getValue() instanceof Defrule) {
            Defrule rl = (Defrule) params[0].getValue();
            if (!engine.getCurrentFocus().containsRule(rl)) {
                add = engine.getRuleCompiler().addRule(rl);
            }
        } else {
            add = Boolean.FALSE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, add);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return DEFRULE;
    }

    /** the input parameter is a single ValueParam containing a Defrule instance. */
    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(defrule <rule-name> (declare (properties)+?) (CE)+ => ([function]))"
                    + ""
                    + "(defrule <rule-name> \"optional_comment\" "
                    + "	(pattern_1) 		; Left-Hand Side (LHS)"
                    + "	(pattern_2) 		; of the rule consisting of elements"
                    + "	...					; before the \"=>\""
                    + "	...					"
                    + "	...					"
                    + "	(pattern_N)"
                    + "	=>"
                    + "	(action_1) 			; Right-Hand Side (RHS)"
                    + "	(action_2) 			; of the rule consisting of elements"
                    + "	...					; after the \"=>\""
                    + "	...					"
                    + "	...					"
                    + "	(action_M)) 		; The last \")\" balances the opening"
                    + "						; \")\" to the left of \"defrule\"."
                    + ""
                    + "Be sure all your parentheses balance or you will get error messages!";
        }
    }
}
