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
package org.morendo.rete.functions.java;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Defclass;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.StringParam;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Peter Lin
 *     <p>SetMemberFunction is equivalent to JESS set-member function. This is a completely clean
 *     implementation from scratch. The name and function signature are similar, but the design and
 *     implementation are different. The design of the function is strongly influenced by CLIPS,
 *     since the primary goal is full CLIPS compatability.
 */
public class SetMemberFunction implements Function {

    /** */
    public static final String SET_MEMBER = "set-member";

    /** */
    public SetMemberFunction() {
        super();
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Function#getReturnType()
     */
    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Function#executeFunction(woolfel.engine.rete.Rete, woolfel.engine.rete.Parameter[])
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (engine != null && params != null && params.length == 3) {
            // every argument may be a literal, a variable or a call
            Object instance = params[0].getValue(engine, ValueType.OBJECT);
            String property = String.valueOf(params[1].getValue(engine, ValueType.OBJECT));
            Object value = params[2].getValue(engine, ValueType.OBJECT);
            Defclass dc = engine.findDefclass(instance);
            // we check to make sure the Defclass exists
            if (dc != null) {
                Method setm = dc.getWriteMethod(property);
                if (setm == null) {
                    throw new IllegalArgumentException(
                            "set-member: "
                                    + dc.getClassObject().getName()
                                    + " has no writable property "
                                    + property);
                }
                try {
                    setm.invoke(
                            instance, new Object[] {toType(value, setm.getParameterTypes()[0])});
                } catch (IllegalAccessException e) {
                    engine.writeMessage(e.getMessage());
                } catch (InvocationTargetException e) {
                    engine.writeMessage(e.getMessage());
                }
            }
        }
        return new DefaultReturnVector();
    }

    /**
     * The value in the type the setter takes: a number is narrowed or widened to the numeric type,
     * anything becomes a String for a String property, and a text becomes a boolean for a boolean
     * one. Other values are passed as they are.
     */
    static Object toType(Object value, Class<?> type) {
        if (value == null || type.isInstance(value)) {
            return value;
        }
        if (value instanceof Number n) {
            if (type == int.class || type == Integer.class) {
                return n.intValue();
            } else if (type == long.class || type == Long.class) {
                return n.longValue();
            } else if (type == double.class || type == Double.class) {
                return n.doubleValue();
            } else if (type == float.class || type == Float.class) {
                return n.floatValue();
            } else if (type == short.class || type == Short.class) {
                return n.shortValue();
            } else if (type == byte.class || type == Byte.class) {
                return n.byteValue();
            } else if (type == BigDecimal.class) {
                return new BigDecimal(n.toString());
            } else if (type == BigInteger.class) {
                return new BigDecimal(n.toString()).toBigInteger();
            }
        }
        if (type == String.class) {
            return String.valueOf(value);
        }
        if ((type == boolean.class || type == Boolean.class) && value instanceof String s) {
            return Boolean.valueOf(s);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Function#getName()
     */
    public String getName() {
        return SET_MEMBER;
    }

    /**
     * The current implementation expects 3 parameters in the following sequence:<br>
     * BoundParam StringParam ValueParam <br>
     * Example: (set-member ?objectVariable slotName value)
     */
    public Class<?>[] getParameter() {
        return new Class<?>[] {BoundParam.class, StringParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        StringBuilder buf = new StringBuilder();
        return buf.toString();
    }
}
