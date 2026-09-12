package org.morendo.rete.functions.io;

import org.morendo.messagerouter.MessageRouter;
import org.morendo.messagerouter.MessageRouter.CommandObject;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class ReadCommandFunction implements Function {

    /** */
    public static final String READ_COMMAND = "read-command";

    public ReadCommandFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object returnObject = null;
        CommandObject command = null;
        MessageRouter router = engine.getMessageRouter();
        while ((command = router.dequeueCommand()) == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                engine.writeMessage(e.getMessage());
            }
        }
        if (command.command() != null) {
            returnObject = command.command();
        }
        // Create the DefaultReturnVector to return the result
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT, returnObject);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return READ_COMMAND;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class};
    }

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(read-command)\n the next command queued on the message router.";
    }
}
