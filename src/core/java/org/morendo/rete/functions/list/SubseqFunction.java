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

import java.util.Arrays;

/** {@code (subseq$ <list> <begin> <end>)}: the elements from begin to end inclusive, 1-based. */
public class SubseqFunction implements Function {

    public static final String SUBSEQ = "subseq$";

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object[] result = new Object[0];
        if (params != null && params.length == 3) {
            Object[] list = Lists.list(engine, params[0]);
            int begin = Math.max(1, Lists.index(engine, params[1]));
            int end = Math.min(list.length, Lists.index(engine, params[2]));
            if (begin <= end) {
                result = Arrays.copyOfRange(list, begin - 1, end);
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.ARRAY, result));
        return ret;
    }

    public String getName() {
        return SUBSEQ;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object[].class, Integer.class, Integer.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(subseq$ <list> <begin> <end>)\n the elements from begin to end inclusive.";
    }
}
