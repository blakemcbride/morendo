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
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 */
public class DefglobalFunction implements Function {

    /** */
    private static String DEFGLOBAL = "defglobal";

    /** */
    public DefglobalFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object value = "false";
        if (params != null && params.length > 0) {
            // ?*a* [= value] ?*b* [= value] ...: a variable followed by anything but a variable
            // takes that as its value, otherwise it is declared as nil
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam name) {
                    value = Constants.NIL_SYMBOL;
                    if (idx + 1 < params.length && !(params[idx + 1] instanceof BoundParam)) {
                        value = params[idx + 1].getValue(engine, ValueType.OBJECT);
                        idx++;
                    }
                    engine.declareDefglobal(name.getVariableName(), value);
                }
            }
        }
        ret.addReturnValue(new DefaultReturnValue(ValueType.OBJECT, value));
        return ret;
    }

    public String getName() {
        return DEFGLOBAL;
    }

    public Class<?>[] getParameter() {
        return null;
    }

    public String toPPString(Parameter[] params, int indents) {
        // TODO Auto-generated method stub
        return "("
                + DEFGLOBAL
                + " ?*<symbol>* [value])\n create a global symbol and optionally assign a value.";
    }
}
