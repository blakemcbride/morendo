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

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class InstanceofFunction implements Function {

    /** */
    public static final String INSTANCEOF = "instanceof";

    private ClassnameResolver classnameResolver;

    public InstanceofFunction(ClassnameResolver classnameResolver) {
        super();
        this.classnameResolver = classnameResolver;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean eval = Boolean.FALSE;
        if (params.length == 2) {
            Object param1 = null;
            if (params[0] instanceof BoundParam && params[1] instanceof BoundParam) {
                param1 = ((BoundParam) params[0]).getObjectRef();
                try {
                    Class<?> clazz =
                            classnameResolver.resolveClass(
                                    ((BoundParam) params[1]).getStringValue());
                    eval = clazz.isInstance(param1);
                } catch (ClassNotFoundException e) {
                    engine.writeMessage(e.getMessage());
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return INSTANCEOF;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {BoundParam.class, BoundParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(instanceof");
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    buf.append(" ?" + bp.getVariableName());
                } else if (params[idx] instanceof ValueParam) {
                    buf.append(" " + params[idx].getStringValue());
                } else {
                    buf.append(" " + params[idx].getStringValue());
                }
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(instanceof <Java-object> <class-name>)\n";
        }
    }
}
