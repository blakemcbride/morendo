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
 */
public class LoadFunctionsFunction implements Function {

    /** */
    public static final String LOAD_FUNCTION = "load-function";

    protected UserDefinedFunctions userDefinedFunctions = null;

    public LoadFunctionsFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean load = Boolean.FALSE;
        DefaultReturnVector ret = new DefaultReturnVector();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                String func = params[idx].getStringValue();
                try {
                    Function f = engine.declareFunction(func);
                    this.userDefinedFunctions.addFunction(f);
                    load = Boolean.TRUE;
                } catch (ClassNotFoundException e) {
                    load = Boolean.TRUE;
                }
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, load);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return LOAD_FUNCTION;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Function#toPPString(woolfel.engine.rete.Parameter[], int)
     */
    public String toPPString(Parameter[] params, int indents) {
        return "(load-function [classname])";
    }

    public UserDefinedFunctions getUserDefinedFunctions() {
        return userDefinedFunctions;
    }

    public void setUserDefinedFunctions(UserDefinedFunctions userDefinedFunctions) {
        this.userDefinedFunctions = userDefinedFunctions;
    }
}
