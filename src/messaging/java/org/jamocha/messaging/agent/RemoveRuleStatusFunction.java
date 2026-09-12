package org.jamocha.messaging.agent;

import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

public class RemoveRuleStatusFunction implements Function {

    /** */
    public static final String REMOVE_RULE_STATUS = "remove-rule-status";

    public RemoveRuleStatusFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length == 7) {
            boolean removed = params[0].getBooleanValue();
            if (removed) {
                String ipaddr = params[1].getStringValue();
                String hostname = params[2].getStringValue();
                String appName = params[3].getStringValue();
                String agentApp = params[4].getStringValue();
                String agentVersion = params[5].getStringValue();
                String rule = params[6].getStringValue();
                String key =
                        ipaddr
                                + ":"
                                + hostname
                                + ":"
                                + appName
                                + ":"
                                + agentApp
                                + ":"
                                + agentVersion;
                AgentEntry agent = AgentRegistry.getAgent(key);
                if (rule.equals("all")) {
                    agent.removeAllRules();
                } else {
                    agent.removeRule(rule);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return REMOVE_RULE_STATUS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String[].class};
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(remove-rule-status <boolean> <ipaddress> <hostname> <application>"
                + " <agentApplicationName> <agentApplicationVersion> <rulename>)";
    }
}
