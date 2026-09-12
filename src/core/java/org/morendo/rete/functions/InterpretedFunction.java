/*
 * Copyright 2002-2007 Peter Lin
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

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.Scope;
import org.morendo.rete.ValueType;

import java.util.HashMap;

/**
 * @author Peter Lin
 */
public class InterpretedFunction implements Function, Scope {

    /** */
    private String name = null;

    protected String ppString = null;
    protected Parameter[] inputParams = null;
    private Function[] internalFunction = null;

    /**
     * these are the functions we pass to the top level function. they may be different than the
     * input parameters for the function.
     */
    private Parameter[][] functionParams = null;

    private HashMap<String, Object> bindings = new HashMap<>();

    /** */
    public InterpretedFunction(
            String name, Parameter[] params, Function[] func, Parameter[][] functionParams) {
        this.name = name;
        this.inputParams = params;
        this.internalFunction = func;
        this.functionParams = functionParams;
    }

    public void configureFunction(Rete engine) {}

    /* (non-Javadoc)
     * @see org.morendo.rete.Function#executeFunction(org.morendo.rete.Rete, org.morendo.rete.Parameter[])
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        // the first thing we do is set the values
        DefaultReturnVector ret = new DefaultReturnVector();
        if (params.length == this.inputParams.length) {
            for (int idx = 0; idx < this.inputParams.length; idx++) {
                BoundParam bp = (BoundParam) this.inputParams[idx];
                this.bindings.put(bp.getVariableName(), params[idx].getValue());
            }
            engine.pushScope(this);
            for (int idx = 0; idx < functionParams.length; idx++) {
                ret =
                        (DefaultReturnVector)
                                this.internalFunction[idx].executeFunction(
                                        engine, this.functionParams[idx]);
            }
            engine.popScope();
            return ret;
        } else {
            DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE);
            ret.addReturnValue(rv);
            DefaultReturnValue rv2 =
                    new DefaultReturnValue(ValueType.STRING, "incorrect number of parameters");
            ret.addReturnValue(rv2);
            return ret;
        }
    }

    public String getName() {
        return this.name;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {BoundParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return ppString;
    }

    public Parameter[] getInputParameters() {
        return inputParams;
    }

    public Parameter[][] getFunctionParams() {
        return functionParams;
    }

    public void setFunctionParams(Parameter[][] functionParams) {
        this.functionParams = functionParams;
    }

    public Object getBindingValue(Object var) {
        return this.bindings.get(var);
    }

    public void setBindingValue(String name, Object value) {
        this.bindings.put(name, value);
    }
}
