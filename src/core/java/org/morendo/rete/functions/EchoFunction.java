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

import org.morendo.rete.BoundParam;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ShellBoundParam;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>EchoFunction is used to echo variable bindings in the shell.
 */
public class EchoFunction implements Function {

    /** */
    public static final String ECHO = "echo";

    /** */
    public EchoFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    /**
     * The method expects an array of ShellBoundParam. The method will use StringBuilder to resolve
     * the binding and print out 1 binding per line.
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        StringBuilder buf = new StringBuilder();
        for (int idx = 0; idx < params.length; idx++) {
            if (params[idx] instanceof ShellBoundParam) {
                ShellBoundParam bp = (ShellBoundParam) params[idx];
                bp.resolveBinding(engine);
                buf.append(bp.getStringValue() + Constants.LINEBREAK);
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.STRING, buf.toString());
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return ECHO;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ShellBoundParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(echo");
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    buf.append(" ?" + bp.getVariableName());
                } else {
                    buf.append(" \"" + params[idx].getStringValue() + "\"");
                }
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(echo [parameter])";
        }
    }
}
