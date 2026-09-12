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

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffacts;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (ppdeffacts <name>)}: pretty-prints a deffacts of the module in focus. */
public class PPDeffactsFunction implements Function {

    public static final String PPDEFFACTS = "ppdeffacts";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean found = Boolean.FALSE;
        if (params != null) {
            for (Parameter p : params) {
                Deffacts dfs = engine.getCurrentFocus().getDeffacts(p.getStringValue());
                if (dfs != null) {
                    engine.writeMessage(dfs.toPPString() + Constants.LINEBREAK, "t");
                    found = Boolean.TRUE;
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, found));
        return ret;
    }

    public String getName() {
        return PPDEFFACTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(ppdeffacts <name>)\n prints a deffacts construct.";
    }
}
