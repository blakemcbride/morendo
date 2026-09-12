package org.jamocha.messaging.agent;

import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

public class AddRuleStatusFunction implements Function {

	/**
	 * 
	 */
	public static final String ADD_RULE_STATUS = "add-rule-status";

	public AddRuleStatusFunction() {
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
				String key = ipaddr + ":" + hostname + ":" + appName + ":" + agentApp + ":" + agentVersion;
				AgentEntry agent = AgentRegistry.getAgent(key);
				agent.addRule(rule);
			}
		}
		DefaultReturnVector ret = new DefaultReturnVector();
		return ret;
	}

	public String getName() {
		return ADD_RULE_STATUS;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{String[].class};
	}

	public ValueType getReturnType() {
		return ValueType.RETURN_VOID;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(add-rule-status <boolean> <ipaddress> <hostname> <application> <agentApplicationName> <agentApplicationVersion>)";
	}

}
