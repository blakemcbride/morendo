/*
 * Copyright 2002-2007 Peter Lin
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
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.Strategy;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.strategies.Strategies;

import java.lang.reflect.InvocationTargetException;

/**
 * Function is used to register a new strategy defined by the user. The user must implement the
 * Strategy interface
 *
 * @author Peter Lin
 */
public class DefstrategyFunction implements Function {

    /** */
    public static final String DEFSTRATEGY = "defstrategy";

    public DefstrategyFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean def = Boolean.TRUE;
        if (params.length == 1) {
            String clazz = params[0].getStringValue();
            Class<?> clzz;
            try {
                clzz = Class.forName(clazz);
                Strategy strat = (Strategy) clzz.getDeclaredConstructor().newInstance();
                Strategies.register(strat);
                def = Boolean.TRUE;
            } catch (ClassNotFoundException e) {
                // for now we do nothing
            } catch (InstantiationException e) {
                // for now we do nothing
            } catch (IllegalAccessException e) {
                // for now we do nothing
            } catch (NoSuchMethodException e) {
                // for now we do nothing
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            def = Boolean.FALSE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, def);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return DEFSTRATEGY;
    }

    /**
     * defclass function expects 3 parameters. (defclass classname, templatename, parenttemplate)
     * parent template name is optional.
     */
    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(defstrategy");
            for (int idx = 0; idx < params.length; idx++) {
                buf.append(" " + params[idx].getStringValue());
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(defstrategy [new classname])";
        }
    }
}
