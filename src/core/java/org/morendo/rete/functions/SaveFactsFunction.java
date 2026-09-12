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
package org.morendo.rete.functions;

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffact;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.util.FactUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Peter Lin
 *     <p>Facts function will printout all the facts, not including any initial facts which are
 *     internal to the rule engine.
 */
public class SaveFactsFunction implements Function {

    /** */
    public static final String SAVE_FACTS = "save-facts";

    /** */
    public SaveFactsFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean saved = Boolean.FALSE;
        boolean sortid = true;
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length >= 1) {
            if (params.length >= 2
                    && params[1] != null
                    && "template".equals(params[1].getStringValue())) {
                sortid = false;
            }
            try {
                FileWriter writer = new FileWriter(params[0].getStringValue());
                List<?> facts = engine.getAllFacts();
                Object[] sorted = null;
                if (sortid) {
                    sorted = FactUtils.sortFacts(facts);
                } else {
                    sorted = FactUtils.sortFactsByTemplate(facts);
                }
                for (int idx = 0; idx < sorted.length; idx++) {
                    Deffact ft = (Deffact) sorted[idx];
                    writer.write(ft.toPPString() + Constants.LINEBREAK);
                }
                writer.close();
                saved = Boolean.TRUE;
            } catch (IOException e) {
                // we should log this
            }
        }
        DefaultReturnValue drv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, saved);
        rv.addReturnValue(drv);
        return rv;
    }

    public String getName() {
        return SAVE_FACTS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(save-facts [filename] [sort(id|template)])";
    }
}
