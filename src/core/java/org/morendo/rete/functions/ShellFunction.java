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
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>The purpose of Shell function is to make it easy to parse text in the shell and execute
 *     the real function. ShellFunction expects the parser to pass the name of the real function and
 *     parameter values.
 */
public class ShellFunction implements Function {

    /** */
    public String funcName = null;

    private Function actualFunction = null;
    private Parameter[] params = null;

    /** */
    public ShellFunction() {
        super();
    }

    public void lookUpFunction(Rete engine) {
        this.actualFunction = engine.findFunction(this.funcName);
    }

    public ValueType getReturnType() {
        return this.actualFunction.getReturnType();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        this.lookUpFunction(engine);
        if (this.params != null && this.actualFunction != null) {
            return this.actualFunction.executeFunction(engine, this.params);
        } else {
            DefaultReturnVector rv = new DefaultReturnVector();
            DefaultReturnValue rval =
                    new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE);
            rv.addReturnValue(rval);
            return rv;
        }
    }

    public String getName() {
        return funcName;
    }

    public Class<?>[] getParameter() {
        return this.actualFunction.getParameter();
    }

    /**
     * The name of the function to call
     *
     * @param name
     */
    public void setName(String name) {
        this.funcName = name;
    }

    public Parameter[] getParameters() {
        return this.params;
    }

    public void setParameters(Parameter[] params) {
        this.params = params;
    }

    public void setFunction(Function func) {
        this.actualFunction = func;
    }

    public Function getFunction() {
        return this.actualFunction;
    }

    public String toPPString(Parameter[] params, int indents) {
        StringBuilder buf = new StringBuilder();
        return buf.toString();
    }
}
