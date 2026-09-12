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
package org.morendo.rete.functions.io;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (close [<router>])}: closes one file router, or every open one. */
public class CloseFunction implements Function {

    public static final String CLOSE = "close";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        boolean closed;
        if (params != null && params.length == 1) {
            closed =
                    engine.closeRouter(
                            String.valueOf(params[0].getValue(engine, ValueType.STRING)));
        } else {
            closed = engine.closeRouter(null);
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, closed));
        return ret;
    }

    public String getName() {
        return CLOSE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(close [<router>])\n closes a file router opened with open, or all of them.";
    }
}
