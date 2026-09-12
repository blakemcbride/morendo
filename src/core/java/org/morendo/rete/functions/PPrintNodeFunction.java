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

import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>The function will print out the rule in a pretty format. Note the format may not be
 *     identicle to what the user wrote. It is a normalized and cleaned up format.
 */
public class PPrintNodeFunction implements Function {

    /** */
    public static final String PPNODE = "ppnode";

    /** */
    public PPrintNodeFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    /** TODO - I need to finish this, so it can print out a node in a pretty format */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {}
        }
        DefaultReturnVector rv = new DefaultReturnVector();
        return rv;
    }

    public String getName() {
        return PPNODE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        StringBuilder buf = new StringBuilder();
        return buf.toString();
    }
}
