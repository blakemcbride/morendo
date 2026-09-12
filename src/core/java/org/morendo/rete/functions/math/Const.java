/*
 * Copyright 2006-2008 Jamocha
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
package org.morendo.rete.functions.math;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * @author Nikolaus Koemm
 */
public class Const implements Function {

    /** */
    public static final String CONST = "const";

    /** */
    public Const() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        double eq = 0;
        if (params[0] != null) {
            String val = params[0].getStringValue();
            if (val.compareTo("pi") == 0) {
                eq = java.lang.Math.PI;
            } else if (val.compareTo("e") == 0) {
                eq = java.lang.Math.E;
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BIG_DECIMAL, Double.valueOf(eq));
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return CONST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(const e|pi)\n"
                + "Function description:\n"
                + "\te  return the value of the Euler constant,\n"
                + "\tpi returns the value of Pi.";
    }
}
