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
import org.morendo.rete.Evaluate;
import org.morendo.rete.Function;
import org.morendo.rete.Operator;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.util.ArrayList;
import java.util.List;

/** {@code (delete-member$ <list> <value>+)}: the list without every occurrence of the values. */
public class DeleteMemberFunction implements Function {

    public static final String DELETE_MEMBER = "delete-member$";

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object[] result = new Object[0];
        if (params != null && params.length >= 2) {
            Object[] list = Lists.list(engine, params[0]);
            List<Object> drop = Lists.values(engine, params, 1);
            List<Object> out = new ArrayList<>();
            for (Object element : list) {
                boolean keep = true;
                for (Object d : drop) {
                    if (Evaluate.evaluate(Operator.EQUAL, element, d)) {
                        keep = false;
                        break;
                    }
                }
                if (keep) {
                    out.add(element);
                }
            }
            result = out.toArray();
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.ARRAY, result));
        return ret;
    }

    public String getName() {
        return DELETE_MEMBER;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object[].class, Object[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(delete-member$ <list> <value>+)\n removes every occurrence of the values.";
    }
}
