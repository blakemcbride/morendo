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

import java.io.IOException;

/**
 * {@code (open <file> <router> ["r"|"w"|"a"])}: opens a file for reading (the default), writing or
 * appending under a router name that {@code printout}, {@code format}, {@code readline} and {@code
 * read} then accept.
 */
public class OpenFunction implements Function {

    public static final String OPEN = "open";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        boolean opened = false;
        if (params != null && params.length >= 2) {
            String file = String.valueOf(params[0].getValue(engine, ValueType.STRING));
            String router = String.valueOf(params[1].getValue(engine, ValueType.STRING));
            String mode =
                    params.length > 2
                            ? String.valueOf(params[2].getValue(engine, ValueType.STRING))
                            : "r";
            try {
                engine.openRouter(router, file, mode);
                opened = true;
            } catch (IOException | IllegalArgumentException e) {
                engine.writeMessage("open: " + e.getMessage() + System.lineSeparator());
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, opened));
        return ret;
    }

    public String getName() {
        return OPEN;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class, String.class, String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(open <file> <router> [\"r\" | \"w\" | \"a\"])\n opens a file as a named router"
                + " for reading (the default), writing or appending.";
    }
}
