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
package org.morendo.messaging.functions;

import org.morendo.messaging.MessageClient;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class SendMessageFunction implements Function {

    /** */
    public static final String SEND_MSG = "send-msg";

    public SendMessageFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean sent = Boolean.FALSE;
        if (params != null && params.length == 2) {
            String client = params[0].getStringValue();
            String message = params[1].getStringValue();
            MessageClient msgClient = (MessageClient) engine.getDefglobalValue(client);
            if (msgClient != null) {
                msgClient.publish(message);
                sent = Boolean.TRUE;
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, sent);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return SEND_MSG;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class, String.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(send-msg <*client instance name*> <message>)";
    }
}
