package org.morendo.messaging.functions;

import org.morendo.messaging.ContentHandler;
import org.morendo.messaging.ContentHandlerRegistry;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.lang.reflect.InvocationTargetException;

public class RegisterContentHandlerFunction implements Function {

    /** */
    public static final String REGISTER_CONTENT_HANDLER = "register-content-handler";

    public RegisterContentHandlerFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean register = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                String name = params[i].getStringValue();
                try {
                    Class<?> clzz = Class.forName(name);
                    ContentHandler handler =
                            (ContentHandler) clzz.getDeclaredConstructor().newInstance();
                    String[] types = handler.getMessageTypes();
                    for (int x = 0; x < types.length; x++) {
                        ContentHandlerRegistry.registerHandler(types[x], handler);
                    }
                    register = Boolean.TRUE;
                } catch (ClassNotFoundException e) {
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                } catch (IllegalArgumentException e) {
                } catch (InvocationTargetException e) {
                } catch (NoSuchMethodException e) {
                } catch (SecurityException e) {
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, register);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return REGISTER_CONTENT_HANDLER;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(register-content-handler <classname>)";
    }
}
