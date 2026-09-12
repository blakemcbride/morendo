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

import org.morendo.rete.Activation;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Fact;
import org.morendo.rete.Function;
import org.morendo.rete.Module;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * {@code (agenda [<module>])}: prints the activations of the module in focus, or of the named
 * module, in the order they would fire: salience, rule name and the ids of the matched facts.
 */
public class AgendaFunction implements Function {

    public static final String AGENDA = "agenda";

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Module mod = engine.getCurrentFocus();
        if (params != null && params.length == 1) {
            String name = String.valueOf(params[0].getValue(engine, ValueType.STRING));
            mod = engine.findModule(name);
            if (mod == null) {
                engine.writeMessage("unknown module " + name + Constants.LINEBREAK, "t");
                return new DefaultReturnVector();
            }
        }
        int count = 0;
        for (Activation act : mod.listActivations()) {
            StringBuilder line = new StringBuilder();
            line.append(act.getRule().getSalience()).append(' ').append(act.getRule().getName());
            line.append(':');
            Fact[] facts = act.getFacts();
            for (int i = 0; i < facts.length; i++) {
                line.append(i == 0 ? " f-" : ",f-").append(facts[i].getFactId());
            }
            engine.writeMessage(line + Constants.LINEBREAK, "t");
            count++;
        }
        engine.writeMessage("for a total of " + count + Constants.LINEBREAK, "t");
        return new DefaultReturnVector();
    }

    public String getName() {
        return AGENDA;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(agenda [<module>])\n prints the activations in firing order: salience, rule and"
                + " fact ids.";
    }
}
