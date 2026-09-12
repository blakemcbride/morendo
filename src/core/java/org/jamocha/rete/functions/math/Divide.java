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
import java.math.RoundingMode;

/**
 * @author Peter Lin
 *     <p>Divide will divide one or more numbers and return a Double value
 */
public class Divide implements Function {

    /** */
    public static final String DIVIDE = "divide";

    /** */
    public Divide() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    /**
     * By default divide uses BigDecimal divide with a precision of 20. this avoids issues with
     * repeating sequences like 1/3
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        BigDecimal bdval = null;
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
                    bdval = bdval.divide(bd, 20, RoundingMode.DOWN);
                } else {
                    BigDecimal bd =
                            new BigDecimal(
                                    params[idx].getValue(engine, ValueType.BIG_DECIMAL).toString());
                    bdval = bdval.divide(bd, 20, RoundingMode.DOWN);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.DOUBLE_PRIM, bdval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return DIVIDE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(/");
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
            return "(/ (<literal> | <binding>)+)\n"
                    + "Function description:\n"
                    + "\t Returns the value of the first argument divided by "
                    + "each of the subsequent arguments.";
        }
    }

    @SuppressWarnings("unused")
    private BigDecimal secureDivide(BigDecimal dividend, BigDecimal divisor) {
        try {
            dividend = dividend.divide(divisor);
        } catch (ArithmeticException e) {
            dividend = new BigDecimal(dividend.doubleValue() / divisor.doubleValue());
        }

        return dividend;
    }
}
