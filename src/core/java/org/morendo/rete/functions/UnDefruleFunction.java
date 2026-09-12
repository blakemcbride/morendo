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

/**
 * @author Peter Lin
 */
public class UnDefruleFunction implements Function {

    /** */
    public static final String UNDEFRULE = "undefrule";

    public UnDefruleFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean removed = Boolean.TRUE;
        if (params.length == 1) {
            String rl = params[0].getStringValue();
            java.util.List<org.morendo.rule.Rule> found = engine.getCurrentFocus().findRules(rl);
            for (org.morendo.rule.Rule defrl : found) {
                engine.getCurrentFocus().removeRule(defrl, engine, engine.getWorkingMemory());
            }
            if (found.isEmpty()) {
                removed = Boolean.FALSE;
            }
        } else {
            removed = Boolean.FALSE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, removed);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return UNDEFRULE;
    }

    /**
     * The expected parameter is a single ValueParam containing a deftemplate instance. The function
     * gets the deftemplate using Parameter.getValue().
     */
    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(undefrule name)";
        }
    }
}
