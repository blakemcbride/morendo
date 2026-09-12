package org.morendo.messaging.functions;

import org.morendo.messaging.BasicClient;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class CreateMessageClientFunction implements Function {

    /** */
    public static final String CREATE_MESSAGE_CLIENT = "create-message-client";

    public CreateMessageClientFunction() {
        super();
    }

    /**
     * <ul>
     *   <li>JNDI InitialContextFactory
     *   <li>Provider URL
     *   <li>connection factory: TopicConnectionFactory
     *   <li>Topic
     *   <li>Username
     *   <li>Password
     *   <li>client name
     * </ul>
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean created = Boolean.FALSE;
        if (params != null && params.length == 7) {
            BasicClient client = new BasicClient();
            client.setRete(engine);
            client.setInitialContextFactory(params[0].getStringValue());
            client.setProviderURL(params[1].getStringValue());
            client.setConnectionFactory(params[2].getStringValue());
            client.setTopic(params[3].getStringValue());
            client.setSecurityPrinciple(params[4].getStringValue());
            client.setSecurityCredentials(params[5].getStringValue());
            client.init();

            String clientName = params[6].getStringValue();
            engine.declareDefglobal(clientName, client);
            created = Boolean.TRUE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, created);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return CREATE_MESSAGE_CLIENT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
        };
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(create-message-client <jndi> <provider url> <connection factory> <topic>"
                + " <username> <password> <*client instance name*>)";
    }
}
