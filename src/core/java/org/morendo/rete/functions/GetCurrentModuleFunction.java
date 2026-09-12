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
import org.morendo.rete.Module;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 */
public class GetCurrentModuleFunction implements Function {

    /** */
    private String GET_CURRENT_MODULE = "get-current-module";

    /** */
    public GetCurrentModuleFunction() {
        super();
    }

    public String getName() {
        return GET_CURRENT_MODULE;
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rvector = new DefaultReturnVector();
        Module module = engine.getCurrentFocus();
        DefaultReturnValue rval = new DefaultReturnValue(ValueType.STRING, module.getModuleName());
        rvector.addReturnValue(rval);
        return rvector;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(get-current-module)";
    }
}
