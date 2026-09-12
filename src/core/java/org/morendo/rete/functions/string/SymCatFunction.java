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
package org.morendo.rete.functions.string;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (sym-cat <value>+)}: concatenates its arguments into a symbol. */
public class SymCatFunction implements Function {

    public static final String SYM_CAT = "sym-cat";

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        StringBuilder buf = new StringBuilder();
        if (params != null) {
            for (Parameter param : params) {
                if (param instanceof BoundParam bp) {
                    bp.resolveBinding(engine);
                }
                Object value = param.getValue(engine, ValueType.STRING);
                if (value != null) {
                    buf.append(value);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.STRING, buf.toString()));
        return ret;
    }

    public String getName() {
        return SYM_CAT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(sym-cat <value>+)\n concatenates the arguments into a symbol; symbols and"
                + " strings are the same type in Morendo.";
    }
}
