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
package org.morendo.rete.functions.type;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * {@code (integer <number>)} truncates to an integer; {@code (float <number>)} makes a floating
 * point value. A string holding a number is accepted; anything else answers {@code false}.
 */
public final class NumberConversionFunction implements Function {

    private final boolean toInteger;

    NumberConversionFunction(boolean toInteger) {
        this.toInteger = toInteger;
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object result = Boolean.FALSE;
        if (params != null && params.length == 1) {
            Object value = params[0].getValue(engine, ValueType.OBJECT);
            BigDecimal number = null;
            if (value instanceof BigDecimal bd) {
                number = bd;
            } else if (value instanceof Number n) {
                number = new BigDecimal(n.toString());
            } else if (value != null) {
                try {
                    number = new BigDecimal(value.toString().trim());
                } catch (NumberFormatException e) {
                    number = null;
                }
            }
            if (number != null) {
                result =
                        this.toInteger
                                ? number.setScale(0, RoundingMode.DOWN)
                                : number.setScale(Math.max(1, number.scale()));
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

    public String getName() {
        return this.toInteger ? "integer" : "float";
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return this.toInteger
                ? "(integer <number>)\n truncates to an integer."
                : "(float <number>)\n converts to a floating point number.";
    }
}
