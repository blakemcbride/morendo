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
import org.morendo.rete.Evaluate;
import org.morendo.rete.Function;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Operator;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 */
public class EqFunction implements Function {

    /** */
    public static final String EQUAL = "eq";

    /** */
    public EqFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Boolean eq = Boolean.FALSE;
        if (params != null && params.length > 1) {
            Object first = null;
            if (params[0] instanceof ValueParam) {
                ValueParam n = (ValueParam) params[0];
                first = n.getValue();
            } else if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                first = bp.getValue();
                if (first == null) {
                    first = engine.getBinding(bp.getVariableName());
                }
            } else if (params[0] instanceof FunctionParam2) {
                FunctionParam2 n = (FunctionParam2) params[0];
                n.setEngine(engine);
                n.lookUpFunction();
                ReturnVector rval = (ReturnVector) n.getValue();
                first = rval.firstReturnValue().getValue();
            }
            Boolean eval = Boolean.TRUE;
            for (int idx = 1; idx < params.length; idx++) {
                Object right = null;
                if (params[idx] instanceof ValueParam) {
                    ValueParam n = (ValueParam) params[idx];
                    right = n.getValue();
                } else if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    right = bp.getValue();
                    if (right == null) {
                        right = engine.getBinding(bp.getVariableName());
                    }
                } else if (params[idx] instanceof FunctionParam2) {
                    FunctionParam2 n = (FunctionParam2) params[idx];
                    n.setEngine(engine);
                    n.lookUpFunction();
                    ReturnVector rval = (ReturnVector) n.getValue();
                    right = rval.firstReturnValue().getValue();
                }
                if (first == null && right != null) {
                    eval = Boolean.FALSE;
                    break;
                } else if (first != null && !Evaluate.evaluate(Operator.EQUAL, first, right)) {
                    eval = Boolean.FALSE;
                    break;
                }
            }
            eq = eval;
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eq);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return EQUAL;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(eq (<literal> | <binding>)+)\n"
                + "Function description:\n"
                + "\tCompares a literal value against one or more"
                + "bindings. \n\tIf all of the bindings are equal to the constant value,"
                + "\n\tthe function returns true.";
    }
}
