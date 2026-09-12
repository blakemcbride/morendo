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

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>PrintProfileFunction will print out the profile information.
 */
public class PrintProfileFunction implements Function {

    /** */
    public static final String PRINT_PROFILE = "print-profile";

    /** */
    public PrintProfileFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        engine.writeMessage(
                "fire ET=" + engine.getProfileStats().fireTime + " ms" + Constants.LINEBREAK, "t");
        engine.writeMessage(
                "assert ET=" + engine.getProfileStats().assertTime + " ms" + Constants.LINEBREAK,
                "t");
        engine.writeMessage(
                "retract ET=" + engine.getProfileStats().retractTime + " ms" + Constants.LINEBREAK,
                "t");
        engine.writeMessage(
                "add Activation ET="
                        + engine.getProfileStats().addActivation
                        + " ms"
                        + Constants.LINEBREAK,
                "t");
        engine.writeMessage(
                "remove Activation ET="
                        + engine.getProfileStats().rmActivation
                        + " ms"
                        + Constants.LINEBREAK,
                "t");
        engine.writeMessage(
                "Activation added=" + engine.getProfileStats().addcount + Constants.LINEBREAK, "t");
        engine.writeMessage(
                "Activation removed=" + engine.getProfileStats().rmcount + Constants.LINEBREAK,
                "t");
        engine.writeMessage(
                "Average cube query="
                        + engine.getProfileStats().averageCubeQueryTime
                        + " ms"
                        + Constants.LINEBREAK,
                "t");
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return PRINT_PROFILE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(print-profile)";
    }
}
