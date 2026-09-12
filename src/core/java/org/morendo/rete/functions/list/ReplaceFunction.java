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

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code (replace$ <list> <begin> <end> <value>+)}: a copy of the list with the elements from begin
 * to end replaced by the values; a list value is spliced in.
 */
public class ReplaceFunction implements Function {

    public static final String REPLACE = "replace$";

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object[] result = new Object[0];
        if (params != null && params.length >= 4) {
            Object[] list = Lists.list(engine, params[0]);
            int begin = Lists.index(engine, params[1]);
            int end = Lists.index(engine, params[2]);
            List<Object> out = new ArrayList<>();
            for (int i = 0; i < Math.min(begin - 1, list.length); i++) {
                out.add(list[i]);
            }
            out.addAll(Lists.values(engine, params, 3));
            for (int i = Math.max(end, 0); i < list.length; i++) {
                out.add(list[i]);
            }
            result = out.toArray();
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.ARRAY, result));
        return ret;
    }

    public String getName() {
        return REPLACE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object[].class, Integer.class, Integer.class, Object[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(replace$ <list> <begin> <end> <value>+)\n replaces the elements from begin to"
                + " end with the values.";
    }
}
