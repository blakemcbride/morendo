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
 *     <p>WatchFunction allows users to watch different engine process, like activations, facts and
 *     rules.
 */
public class UnWatchFunction implements Function {

    /** */
    protected static final String UNWATCH = "unwatch";

    /** */
    public UnWatchFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null) {
            // the params are not null, now check the parameter count
            if (params.length > 0) {
                for (int idx = 0; idx < params.length; idx++) {
                    String cmd = params[idx].getStringValue();
                    setWatch(engine, cmd);
                }
            } else {
                // we do nothing, maybe we should return a message
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    protected void setWatch(Rete engine, String cmd) {
        if (cmd.equals("all")) {
            engine.setUnWatch(Rete.Watch.ALL);
        } else if (cmd.equals("facts")) {
            engine.setUnWatch(Rete.Watch.FACTS);
        } else if (cmd.equals("activations")) {
            engine.setUnWatch(Rete.Watch.ACTIVATIONS);
        } else if (cmd.equals("rules")) {
            engine.setUnWatch(Rete.Watch.RULES);
        }
    }

    public String getName() {
        return UNWATCH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(unwatch)";
    }
}
