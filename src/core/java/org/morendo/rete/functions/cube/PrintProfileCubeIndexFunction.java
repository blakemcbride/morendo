/*
 * Copyright 2002-2009 Jamocha
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
package org.morendo.rete.functions.cube;

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rete.util.ProfileStats;

public class PrintProfileCubeIndexFunction implements Function {

    /** */
    public static final String PRINT_PROFILE_CUBE_INDEX = "print-profile-cube-index";

    public PrintProfileCubeIndexFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        engine.writeMessage(
                "index Cube ET=" + ProfileStats.indexTime + " ms" + Constants.LINEBREAK, "t");
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return PRINT_PROFILE_CUBE_INDEX;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(print-profile-cube-index)";
    }
}
