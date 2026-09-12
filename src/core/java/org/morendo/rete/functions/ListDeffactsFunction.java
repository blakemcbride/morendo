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
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffacts;
import org.morendo.rete.Function;
import org.morendo.rete.Module;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (list-deffacts)}: prints the deffacts of every module. */
public class ListDeffactsFunction implements Function {

    public static final String LIST_DEFFACTS = "list-deffacts";

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        int count = 0;
        for (Module mod : engine.getWorkingMemory().getModules()) {
            for (Deffacts dfs : mod.getAllDeffacts()) {
                StringBuilder line = new StringBuilder();
                if (!Constants.MAIN_MODULE.equals(mod.getModuleName())) {
                    line.append(mod.getModuleName()).append("::");
                }
                line.append(dfs.getName());
                if (!dfs.getComment().isEmpty()) {
                    line.append(" \"").append(dfs.getComment()).append('"');
                }
                engine.writeMessage(line + Constants.LINEBREAK, "t");
                count++;
            }
        }
        engine.writeMessage("for a total of " + count + Constants.LINEBREAK, "t");
        return new DefaultReturnVector();
    }

    public String getName() {
        return LIST_DEFFACTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(list-deffacts)\n prints the name and comment of every deffacts.";
    }
}
