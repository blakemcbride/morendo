/*
 * Copyright 2002-2010 Peter Lin
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
package org.morendo.rete.functions.java;

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
public class DefclassFunction implements Function {

    /** */
    public static final String DEFCLASS = "defclass";

    public DefclassFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean def = Boolean.TRUE;
        if (params.length >= 0) {
            String clazz = params[0].getStringValue();
            String template = null;
            if (params.length >= 2 && params[1] != null) {
                template = params[1].getStringValue();
            }
            String parent = null;
            if (params.length == 3) {
                parent = params[2].getStringValue();
            }
            try {
                engine.declareObject(clazz, template, parent);
            } catch (ClassNotFoundException e) {
                def = Boolean.FALSE;
            }
        } else {
            def = Boolean.FALSE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, def);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return DEFCLASS;
    }

    /**
     * defclass function expects 3 parameters. (defclass classname, templatename, parenttemplate)
     * parent template name is optional.
     */
    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(defclass");
            for (int idx = 0; idx < params.length; idx++) {
                buf.append(" " + params[idx].getStringValue());
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(defclass [new classname] [template] [parent template])";
        }
    }
}
