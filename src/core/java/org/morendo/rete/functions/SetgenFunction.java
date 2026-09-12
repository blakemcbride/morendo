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
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;

/** {@code (setgen <integer>)}: sets the number the next {@code gensym} uses. */
public class SetgenFunction implements Function {

    public static final String SETGEN = "setgen";

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object result = Boolean.FALSE;
        if (params != null
                && params.length == 1
                && engine.findFunction(GensymFunction.GENSYM) instanceof GensymFunction gensym) {
            Object value = params[0].getValue(engine, ValueType.OBJECT);
            long n =
                    value instanceof Number number
                            ? number.longValue()
                            : new BigDecimal(String.valueOf(value).trim()).longValue();
            gensym.setCounter(n);
            result = BigDecimal.valueOf(n);
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
        return SETGEN;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Long.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(setgen <integer>)\n sets the number the next gensym uses.";
    }
}
