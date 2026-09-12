/*
 * Copyright 2002-2006 Peter Lin
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
package org.morendo.rete.functions.io;

import org.morendo.parser.clips.CLIPSParser;
import org.morendo.parser.clips.ParseException;
import org.morendo.rete.BoundParam;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffact;
import org.morendo.rete.Deftemplate;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.util.IOUtilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Peter Lin LoadFunction will create a new instance of CLIPSParser and load the facts in
 *     the data file.
 */
public class LoadFactsFunction implements Function {

    /** */
    public static final String LOAD = "load-facts";

    /** */
    public LoadFactsFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        Boolean loaded = Boolean.TRUE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                String input = null;
                if (params[idx] instanceof ValueParam vp) {
                    input = vp.getStringValue();
                } else if (params[idx] instanceof BoundParam bp) {
                    Object value = bp.getValue(engine, ValueType.STRING);
                    input = value == null ? null : value.toString();
                }
                if (input == null) {
                    loaded = Boolean.FALSE;
                    continue;
                }
                input = input.replace('\\', '/');
                // check to see if the path is an absolute windows path
                // or absolute unix path
                if (input.indexOf(":") < 0 && !input.startsWith("/") && !input.startsWith("./")) {
                    input = "./" + input;
                }
                try {
                    InputStream inStream = getInputStream(input);
                    CLIPSParser parser = new CLIPSParser(inStream);
                    List<?> data = parser.loadExpr();
                    for (Object val : data) {
                        ValueParam[] vp = (ValueParam[]) val;
                        Deftemplate tmpl =
                                (Deftemplate)
                                        engine.getCurrentFocus()
                                                .getTemplate(vp[0].getStringValue());
                        Deffact fact = (Deffact) tmpl.createFact((Object[]) vp[1].getValue(), -1);

                        engine.assertFact(fact);
                    }
                } catch (FileNotFoundException e) {
                    loaded = Boolean.FALSE;
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                } catch (ParseException e) {
                    loaded = Boolean.FALSE;
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                } catch (AssertException e) {
                    loaded = Boolean.FALSE;
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                } catch (IOException e) {
                    loaded = Boolean.FALSE;
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                }
            }
        } else {
            loaded = Boolean.FALSE;
        }
        DefaultReturnValue drv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, loaded);
        rv.addReturnValue(drv);
        return rv;
    }

    public String getName() {
        return LOAD;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public static InputStream getInputStream(String input)
            throws FileNotFoundException, IOException {
        InputStream inStream = null;
        inStream = IOUtilities.open(input);
        return inStream;
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(load-facts");
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    buf.append(" ?" + bp.getVariableName());
                } else if (params[idx] instanceof ValueParam) {
                    buf.append(" \"" + params[idx].getStringValue() + "\"");
                }
            }
            buf.append(")");
            return buf.toString();
        } else {
            return "(load-facts <filename>)\n"
                    + "Command description:\n"
                    + "\tLoad the facts in the file <filename>.";
        }
    }
}
