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
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Predicate;

/** One of the type tests: {@code numberp}, {@code integerp}, {@code stringp} and so on. */
public final class TypePredicateFunction implements Function {

    private final String name;
    private final String description;
    private final Predicate<Object> test;

    TypePredicateFunction(String name, String description, Predicate<Object> test) {
        this.name = name;
        this.description = description;
        this.test = test;
    }

    static boolean isInteger(Object value) {
        return value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte
                || value instanceof BigInteger
                || (value instanceof BigDecimal bd && bd.scale() <= 0);
    }

    static boolean isFloat(Object value) {
        return value instanceof Double
                || value instanceof Float
                || (value instanceof BigDecimal bd && bd.scale() > 0);
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        boolean result = false;
        if (params != null && params.length == 1) {
            // a literal is taken as written: the typed accessor turns "1" into a number
            Object value =
                    params[0] instanceof ValueParam vp
                            ? vp.getValue()
                            : params[0].getValue(engine, ValueType.OBJECT);
            result = this.test.test(value);
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, result));
        return ret;
    }

    public String getName() {
        return this.name;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(" + this.name + " <value>)\n " + this.description;
    }
}
