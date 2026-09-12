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
 *     <p>ProfileFunction is used to turn on profiling. It provides basic profiling of assert,
 *     retract, add activation, remove activation and fire.
 */
public class ProfileFunction implements Function {

    /** */
    public static final String PROFILE = "profile";

    /** */
    public ProfileFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx].getStringValue().equals("all")) {
                    engine.setProfile(Rete.Profile.ALL);
                } else if (params[idx].getStringValue().equals("assert-fact")) {
                    engine.setProfile(Rete.Profile.ASSERT);
                } else if (params[idx].getStringValue().equals("add-activation")) {
                    engine.setProfile(Rete.Profile.ADD_ACTIVATION);
                } else if (params[idx].getStringValue().equals("fire")) {
                    engine.setProfile(Rete.Profile.FIRE);
                } else if (params[idx].getStringValue().equals("retract-fact")) {
                    engine.setProfile(Rete.Profile.RETRACT);
                } else if (params[idx].getStringValue().equals("remove-activation")) {
                    engine.setProfile(Rete.Profile.RM_ACTIVATION);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return PROFILE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(profile all|assert-fact|retract-fact|fire|add-activation|remove-activation)";
    }
}
