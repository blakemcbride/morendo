package org.jamocha.rete.functions.io;

import org.jamocha.messagerouter.MessageRouter;
import org.jamocha.messagerouter.MessageRouter.CommandObject;
import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

public class ReadFunction implements Function {

	/**
	 * 
	 */
	public static final String READ = "read";
	
	public ReadFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		Object returnObject = null;
		CommandObject command = null;
		MessageRouter router = engine.getMessageRouter();
		while ( (command = router.dequeueCommand()) == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				engine.writeMessage(e.getMessage());
			}
		}
		if (command.getCommand() != null) {
			returnObject = command.getCommand();
		}
		// Create the DefaultReturnVector to return the result
		DefaultReturnVector ret = new DefaultReturnVector();
		DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT,
				returnObject);
		ret.addReturnValue(rv);
		return ret;
	}

	public String getName() {
		return READ;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{Object.class};
	}

	public ValueType getReturnType() {
		return ValueType.OBJECT;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(" + READ + ")" +" return next token from input stream";
	}

}
