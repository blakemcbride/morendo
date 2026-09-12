/*
 * Copyright 2006-2008 Jamocha
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
 * @author Peter Lin
 *     <p>If its ony argument is odd, Oddp returns true.
 */
public class Oddp implements Function {

    /** */
    public static final String ODDP = "oddp";

    public Oddp() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        BigDecimal bdval = new BigDecimal(0);
        Boolean eval = Boolean.FALSE;
        if (params.length == 1) {
            bdval = (BigDecimal) params[0].getValue(engine, ValueType.BIG_DECIMAL);
            double bdh = bdval.doubleValue();
            if (bdh % 2 == 1) {
                eval = Boolean.TRUE;
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return ODDP;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length >= 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(oddp");
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
            return "(oddp <expression>)\n"
                    + "Function description:\n"
                    + "\tReturns true, if its only argument is odd.";
        }
    }
}
