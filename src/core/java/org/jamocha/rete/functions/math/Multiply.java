/*
 * Copyright 2002-2008 Jamocha
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
package org.jamocha.rete.functions.math;

import org.jamocha.rete.BoundParam;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

import java.math.BigDecimal;

/**
 * @author Peter Lin
 */
public class Multiply implements Function {

    /** */
    public static final String MULTIPLY = "multiply";

    public Multiply() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        BigDecimal bdval = new BigDecimal(0);
        if (params != null) {
            if (params[0] instanceof ValueParam) {
                bdval = params[0].getBigDecimalValue();
            } else {
                bdval =
                        new BigDecimal(
                                params[0].getValue(engine, ValueType.BIG_DECIMAL).toString());
            }
            for (int idx = 1; idx < params.length; idx++) {
                if (params[idx] instanceof ValueParam) {
                    ValueParam n = (ValueParam) params[idx];
                    BigDecimal bd = n.getBigDecimalValue();
                    bdval = bdval.multiply(bd);
                } else {
                    BigDecimal bd =
                            new BigDecimal(
                                    params[idx].getValue(engine, ValueType.BIG_DECIMAL).toString());
                    bdval = bdval.multiply(bd);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BIG_DECIMAL, bdval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return MULTIPLY;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(*");
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    buf.append(" ?" + bp.getVariableName());
                } else if (params[idx] instanceof ValueParam) {
                    buf.append(" " + params[idx].getStringValue());
                } else {
                    buf.append(" " + params[idx].getStringValue());
                }
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(* (<literal> | <binding>)+)\n"
                    + "Function description:\n"
                    + "\tCalculates the product of its arguments.";
        }
    }
}
