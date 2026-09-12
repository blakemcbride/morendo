package org.jamocha.messaging.functions;

import org.jamocha.messaging.MessageClient;
import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

public class CloseMessagingClientFunction implements Function {

	/**
	 * 
	 */
	public static final String CLOSE_MSG_CLIENT = "close-message-client";

	public CloseMessagingClientFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		if (params != null && params.length > 0) {
			for (int i=0; i < params.length; i++) {
				Object value = engine.getDefglobalValue(params[i].getStringValue());
				if (value instanceof MessageClient messageClient) {
					(messageClient).close();
					engine.removeDefglobal(params[i].getStringValue());
				}
			}
		}
		DefaultReturnVector ret = new DefaultReturnVector();
		return ret;
	}

	public String getName() {
		return CLOSE_MSG_CLIENT;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{String.class};
	}

	public ValueType getReturnType() {
		return ValueType.RETURN_VOID;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(" + CLOSE_MSG_CLIENT + " <client name>)";
	}

}
