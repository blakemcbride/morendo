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

import org.morendo.rete.Cube;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class ProfileCubeIndexFunction implements Function {

    /** */
    public static final String PROFILE_CUBE_INDEX = "profile-cube-index";

    public ProfileCubeIndexFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean profile = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                String cubename = params[idx].getStringValue();
                Cube c = engine.getCube(cubename);
                if (c != null) {
                    c.setProfileIndex(true);
                    profile = Boolean.TRUE;
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, profile);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return PROFILE_CUBE_INDEX;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(profile-cube-index <cube>)";
    }
}
