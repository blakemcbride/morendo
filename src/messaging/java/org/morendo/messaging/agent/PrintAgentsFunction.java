/*
 * Copyright 2002-2010 Jamocha
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
package org.morendo.messaging.agent;

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public class PrintAgentsFunction implements Function {

    /** */
    public static final String PRINT_AGENTS = "pprint-agents";

    public PrintAgentsFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        List<?> agents = AgentRegistry.getAgents();
        Iterator<?> iterator = agents.iterator();
        while (iterator.hasNext()) {
            AgentEntry agent = (AgentEntry) iterator.next();
            String message =
                    "Agent: "
                            + Constants.LINEBREAK
                            + "  ip address: "
                            + agent.getIPAddress()
                            + Constants.LINEBREAK
                            + "  hostname: "
                            + agent.getHostname()
                            + Constants.LINEBREAK
                            + "  Application: "
                            + agent.getApplication()
                            + Constants.LINEBREAK
                            + "  Agent Application Name: "
                            + agent.getAgentApplicationName()
                            + Constants.LINEBREAK
                            + "  Agent Application Version: "
                            + agent.getAgentApplicationVersion()
                            + Constants.LINEBREAK
                            + "  Timestamp: "
                            + Instant.ofEpochMilli(agent.getTimestamp())
                            + Constants.LINEBREAK;
            engine.writeMessage(message, "t");
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return PRINT_AGENTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(pprint-agents)";
    }
}
