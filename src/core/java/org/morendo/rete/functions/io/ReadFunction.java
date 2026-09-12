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
import org.morendo.rete.functions.string.StringToFieldFunction;

/**
 * {@code (read [<router>])}: reads a line from a file router or the terminal and returns its first
 * field converted as {@code string-to-field} does; the symbol {@code EOF} at the end.
 */
public class ReadFunction implements Function {

    public static final String READ = "read";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        String router =
                params != null && params.length == 1
                        ? String.valueOf(params[0].getValue(engine, ValueType.STRING))
                        : "t";
        String line = engine.readLine(router);
        Object value = line == null ? "EOF" : StringToFieldFunction.field(line);
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.OBJECT, value));
        return ret;
    }

    public String getName() {
        return READ;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(read [<router>])\n reads the first field of the next line from a file router"
                + " or the terminal (t); EOF at the end.";
    }
}
