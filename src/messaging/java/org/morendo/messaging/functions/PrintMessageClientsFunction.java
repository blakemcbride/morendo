package org.morendo.messaging.functions;

import org.morendo.messaging.MessageClient;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.DefglobalMap;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.util.Iterator;

public class PrintMessageClientsFunction implements Function {

    /** */
    public static final String PRINT_MSG_CLIENTS = "pprint-msg-clients";

    public PrintMessageClientsFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefglobalMap globals = engine.getDefglobalMap();
        Iterator<?> iterator = globals.getValueIterator();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            if (value instanceof MessageClient client) {
                String msg =
                        "InitialContextFactory: "
                                + client.getInitialContextFactory()
                                + Constants.LINEBREAK
                                + "  ConnectionFactory: "
                                + client.getConnectionFactory()
                                + Constants.LINEBREAK
                                + "  URL: "
                                + client.getProviderURL()
                                + Constants.LINEBREAK
                                + "  Topic: "
                                + client.getTopic()
                                + Constants.LINEBREAK
                                + "  User: "
                                + client.getSecurityCredentials()
                                + Constants.LINEBREAK
                                + "  name: "
                                + client.getName()
                                + Constants.LINEBREAK;
                engine.writeMessage(msg, "t");
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return PRINT_MSG_CLIENTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(pprint-msg-clients)";
    }
}
