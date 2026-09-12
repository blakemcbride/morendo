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
 * @author Sebastian Reinartz
 */
public class FocusFunction implements Function {

    /** */
    public static final String FOCUS = "focus";

    public FocusFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        String focus = null;
        if (params.length == 0) {
            focus = engine.getCurrentFocus().getModuleName();
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.STRING, focus);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return FOCUS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(focus)";
    }
}
