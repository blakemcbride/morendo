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

/** {@code (deffacts <name> ["comment"] <fact>+)}: records facts for {@code (reset)} to assert. */
public class DeffactsFunction implements Function {

    public static final String DEFFACTS = "deffacts";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean added = Boolean.FALSE;
        if (params != null && params.length == 1 && params[0].getValue() instanceof Deffacts dfs) {
            engine.getCurrentFocus().addDeffacts(dfs);
            added = Boolean.TRUE;
        } else {
            engine.writeMessage(
                    "deffacts expects a name and at least one fact" + Constants.LINEBREAK);
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, added));
        return ret;
    }

    public String getName() {
        return DEFFACTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Deffacts.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length == 1 && params[0].getValue() instanceof Deffacts dfs) {
            return dfs.toPPString();
        }
        return "(deffacts <name> [\"comment\"] (<template> (<slot> <value>+)+)+)"
                + "\n facts that (reset) asserts, in the order defined.";
    }
}
