/*
 * Copyright 2026 Blake McBride
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.functions.math;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** {@code (div <number> <number>+)}: integer division, truncating toward zero. */
public class Div implements Function {

    public static final String DIV = "div";

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object result = Boolean.FALSE;
        if (params != null && params.length >= 2) {
            try {
                BigDecimal value = number(engine, params[0]).setScale(0, RoundingMode.DOWN);
                for (int i = 1; i < params.length; i++) {
                    BigDecimal divisor = number(engine, params[i]).setScale(0, RoundingMode.DOWN);
                    value = value.divideToIntegralValue(divisor).setScale(0, RoundingMode.DOWN);
                }
                result = value;
            } catch (ArithmeticException | NumberFormatException e) {
                engine.writeMessage("div: " + e.getMessage() + System.lineSeparator());
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(
                new DefaultReturnValue(
                        result instanceof BigDecimal
                                ? ValueType.BIG_DECIMAL
                                : ValueType.BOOLEAN_OBJECT,
                        result));
        return ret;
    }

    static BigDecimal number(Rete engine, Parameter param) {
        Object value = param.getValue(engine, ValueType.OBJECT);
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(String.valueOf(value).trim());
    }

    public String getName() {
        return DIV;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {BigDecimal.class, BigDecimal[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(div <number> <number>+)\n integer division, truncating toward zero.";
    }
}
