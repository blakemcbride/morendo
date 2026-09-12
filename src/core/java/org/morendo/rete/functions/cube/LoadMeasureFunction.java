/*
 * Copyright 2002-2009 Jamocha
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
package org.morendo.rete.functions.cube;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rete.measures.Measure;

import java.lang.reflect.InvocationTargetException;

public class LoadMeasureFunction implements Function {

    /** */
    public static final String LOAD_MEASURE = "load-measure";

    public LoadMeasureFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean load = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                String className = params[idx].getStringValue();
                try {
                    Class<?> clzz = Class.forName(className);
                    Measure measure = (Measure) clzz.getDeclaredConstructor().newInstance();
                    engine.declareMeasure(measure);
                    load = Boolean.TRUE;
                } catch (ClassNotFoundException e) {
                    engine.writeMessage(
                            "Could not find the class. Please double check and make sure it is the"
                                    + " fully qualified class name.");
                } catch (InstantiationException e) {
                    engine.writeMessage("Could not create new instance of the class.");
                } catch (IllegalAccessException e) {
                    engine.writeMessage("Could not create new instance of the class.");
                } catch (IllegalArgumentException e) {
                    engine.writeMessage(e.getMessage());
                } catch (InvocationTargetException e) {
                    engine.writeMessage(e.getMessage());
                } catch (NoSuchMethodException e) {
                    engine.writeMessage(e.getMessage());
                } catch (SecurityException e) {
                    engine.writeMessage(e.getMessage());
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, load);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return LOAD_MEASURE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String[].class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(load-measure <class>+)";
    }
}
