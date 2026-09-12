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
package org.morendo.rete.functions.io;

import org.morendo.parser.clips.CLIPSParser;
import org.morendo.parser.clips.ParseException;
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
import org.morendo.rule.*;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author Peter Lin
 *     <p>Functional equivalent of (batch file.clp) in CLIPS and JESS.
 */
public class BuildFunction implements Function {

    /** */
    public static final String BUILD = "build";

    /** */
    public BuildFunction() {
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
        if (params != null && params.length == 1) {
            String input = params[0].getStringValue();
            StringReader reader = new StringReader(input);
            this.parse(engine, reader, rv);
            reader.close();
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
    public void parse(Rete engine, Reader reader, DefaultReturnVector rv) {
        try {
            CLIPSParser parser = new CLIPSParser(engine, reader);
            Object expr = null;
            while ((expr = parser.basicExpr()) != null) {
                if (expr instanceof Defrule rl) {
                    engine.getRuleCompiler().addRule(rl);
                } else if (expr instanceof Deftemplate dft) {
                    engine.getCurrentFocus().addTemplate(dft, engine, engine.getWorkingMemory());
                } else if (expr instanceof Function fnc) {
                    fnc.executeFunction(engine, null);
                }
            }
            if (rv != null) {
                rv.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.TRUE));
            }
        } catch (ParseException e) {
            engine.writeMessage(e.getMessage() + Constants.LINEBREAK, Constants.DEFAULT_OUTPUT);
        }
    }

    public String getName() {
        return BUILD;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(build)";
    }
}
