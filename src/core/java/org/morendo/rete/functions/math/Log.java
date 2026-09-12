/*
 * Copyright 2006-2008 Peter Lin
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
package org.morendo.rete.functions.math;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;

/**
 * @author Nikolaus Koemm
 *     <p>Log returns the natural logarithm of a double value.
 */
public class Log implements Function {

    /** */
    public static final String LOG = "log";

    public Log() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        BigDecimal bdval = new BigDecimal(0);
        if (params.length == 1) {
            if (params[0] instanceof ValueParam) {
                ValueParam n = (ValueParam) params[0];
                bdval = n.getBigDecimalValue();
            } else {
                bdval =
                        new BigDecimal(
                                params[0].getValue(engine, ValueType.BIG_DECIMAL).toString());
            }
            double bdh = Math.log(bdval.doubleValue()); // .pow(Math.E, bdval.doubleValue());
            bdval = BigDecimal.valueOf(bdh);
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BIG_DECIMAL, bdval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return LOG;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length >= 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(log");
            int idx = 0;
            if (params[idx] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[idx];
                buf.append(" ?" + bp.getVariableName());
            } else if (params[idx] instanceof ValueParam) {
                buf.append(" " + params[idx].getStringValue());
            } else {
                buf.append(" " + params[idx].getStringValue());
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(log <expression>)\n"
                    + "Function description:\n"
                    + "\tCalculates the natural logarithm of its argument.";
        }
    }
}
