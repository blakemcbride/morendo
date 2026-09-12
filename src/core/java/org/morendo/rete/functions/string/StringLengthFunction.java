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
package org.morendo.rete.functions.string;

import org.morendo.rete.BoundParam;
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
public class StringLengthFunction implements Function {

    /** */
    public static final String STRING_LENGTH = "str-length";

    /** */
    public StringLengthFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        int len = 0;
        if (params != null && params.length == 1) {
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }
            String txt = params[0].getStringValue();
            len = txt.length();
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv =
                new DefaultReturnValue(ValueType.INTEGER_OBJECT, Integer.valueOf(len));
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return STRING_LENGTH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(str-length [string])";
    }
}
