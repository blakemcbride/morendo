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
package org.morendo.rete.functions.list;

import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Argument handling shared by the list functions added with the standard function set. */
final class Lists {

    private Lists() {}

    /** The argument as a list: an array or List as is, nil as empty, anything else alone. */
    static Object[] list(Rete engine, Parameter param) {
        Object value = param.getValue(engine, ValueType.OBJECT);
        if (value instanceof Object[] array) {
            return array;
        } else if (value instanceof List<?> list) {
            return list.toArray();
        } else if (value == null) {
            return new Object[0];
        }
        return new Object[] {value};
    }

    /** A 1-based index argument. */
    static int index(Rete engine, Parameter param) {
        Object value = param.getValue(engine, ValueType.OBJECT);
        if (value instanceof Number n) {
            return n.intValue();
        }
        return new BigDecimal(String.valueOf(value).trim()).intValue();
    }

    /** The values from the argument onward, list arguments spliced in. */
    static List<Object> values(Rete engine, Parameter[] params, int from) {
        List<Object> values = new ArrayList<>();
        for (int i = from; i < params.length; i++) {
            Object value = params[i].getValue(engine, ValueType.OBJECT);
            if (value instanceof Object[] array) {
                for (Object v : array) {
                    values.add(v);
                }
            } else {
                values.add(value);
            }
        }
        return values;
    }
}
