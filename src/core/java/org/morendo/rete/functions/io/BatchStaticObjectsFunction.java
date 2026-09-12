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

import org.morendo.rete.BoundParam;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deftemplate;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.util.IOUtilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * @author Peter Lin
 *     <p>Functional will load a List<Object> from binary format and assert each object. I assumes
 *     the data file is in binary format and the root object is List<Object>.
 */
public class BatchStaticObjectsFunction implements Function {

    /** */
    public static final String BATCH = "batch-static-objects";

    /** */
    public BatchStaticObjectsFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    /**
     * method will attempt to load one or more files. If batch is called without any parameters, the
     * function does nothing and just returns.
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                try {
                    String input = params[idx].getStringValue();
                    InputStream inStream;
                    inStream = IOUtilities.open(input);
                    this.parse(engine, inStream, rv);
                    inStream.close();
                } catch (FileNotFoundException e) {
                    // we should report the error
                    rv.addReturnValue(
                            new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE));
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                } catch (IOException e) {
                    rv.addReturnValue(
                            new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE));
                    engine.writeMessage(
                            e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
                }
            }
        }
        return rv;
    }

    /**
     * method does the actual work of creating a CLIPSParser and parsing the file.
     *
     * @param engine
     * @param ins
     * @param rv
     */
    public void parse(Rete engine, InputStream ins, DefaultReturnVector rv) {
        try {
            ObjectInputStream ois = new ObjectInputStream(ins);
            @SuppressWarnings("unchecked")
            List<Object> data = (List<Object>) ois.readObject();
            for (Object obj : data) {
                Deftemplate templ = engine.findDeftemplate(obj.getClass());
                engine.assertObject(obj, templ.getName(), true, true);
            }
            if (rv != null) {
                rv.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.TRUE));
            }
        } catch (Exception e) {
            engine.writeMessage(e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
        }
    }

    public String getName() {
        return BATCH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null && params.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("(batch-static-objects");
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
            return "(batch-static-objects <filename>)\n"
                    + "Command description:\n"
                    + "\tLoads and executes the file <filename>.";
        }
    }
}
