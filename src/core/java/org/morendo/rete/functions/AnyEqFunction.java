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
 * @author Peter Lin Any equal is used to compare a literal value against one or more bindings. If
 *     any of the bindings is equal to the constant value, the function returns true.
 */
public class AnyEqFunction implements Function {

    /** */
    public static final String ANYEQUAL = "any-eq";

    /** */
    public AnyEqFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Boolean eq = Boolean.FALSE;
        if (params != null && params.length > 1) {
            Object constant = params[0].getValue(engine, ValueType.OBJECT);
            for (int idx = 1; idx < params.length; idx++) {
                if (constant.equals(params[idx].getValue(engine, ValueType.OBJECT))) {
                    eq = Boolean.TRUE;
                    break;
                }
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eq);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return ANYEQUAL;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, BoundParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(any-eq (<literal> | <binding>)+)\n"
                + "Function description:\n"
                + "\tCompares a literal value against one or more"
                + "bindings. \n\tIf any of the bindings is equal to the constant value,"
                + "\n\tthe function returns true.";
    }
}
