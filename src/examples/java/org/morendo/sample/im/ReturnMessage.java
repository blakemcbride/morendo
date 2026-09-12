package org.morendo.sample.im;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.io.Serializable;

/**
 * ReturnMessage is a dummy function and isn't implemented. It's here so that people can run the
 * sample rules in Jamocha. To make it work for real, the executeFunction method needs to be
 * implemented.
 *
 * @author Peter Lin
 */
public class ReturnMessage implements Function, Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    public static final String RETURN_MESSAGE = "return-msg";

    public ReturnMessage() {
        super();
    }

    /**
     * the method is not implemented. to get it to work for real, the method needs to get a JMS
     * client and send the message to a return queue.
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length == 1) {
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                Message msg = (Message) bp.getObjectRef();
                msg.setMessageStatus(Message.RETURNED);
                System.out.println("Message returned, user does not exist");
            }
        }
        return rv;
    }

    public String getName() {
        return RETURN_MESSAGE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class};
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(return-msg <Object>)";
    }
}
