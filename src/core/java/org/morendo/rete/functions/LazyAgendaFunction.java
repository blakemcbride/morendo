/*
 * Copyright 2002-2006 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>LazyAgenda is used to turn on/off lazy agenda. That means the activations are not sorted
 *     when added to the agenda. Instead, it's sorted when they are removed.
 */
public class LazyAgendaFunction implements Function {

    /** */
    public static final String LAZY_AGENDA = "lazy-agenda";

    /** */
    public LazyAgendaFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        // boolean exec = false; Unused
        String mode = "normal";
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length == 1) {
            // exec = true; Unused
            ValueParam vp = (ValueParam) params[0];
            if (vp.getStringValue().equals("true")) {
                engine.getCurrentFocus().setLazy(true);
                mode = "lazy";
            } else if (vp.getStringValue().equals("false")) {
                engine.getCurrentFocus().setLazy(false);
            }
        }
        DefaultReturnValue drv = new DefaultReturnValue(ValueType.STRING, mode);
        rv.addReturnValue(drv);
        return rv;
    }

    public String getName() {
        return LAZY_AGENDA;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(lazy-agenda [on|off])";
    }
}
