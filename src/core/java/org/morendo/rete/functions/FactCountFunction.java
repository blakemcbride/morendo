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

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>Facts function will printout all the facts, not including any initial facts which are
 *     internal to the rule engine.
 */
public class FactCountFunction implements Function {

    /** */
    public static final String FACT_COUNT = "fact-count";

    /** */
    public FactCountFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        int count = 0;
        count = engine.getAllFacts().size();
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv =
                new DefaultReturnValue(ValueType.INTEGER_OBJECT, Integer.valueOf(count));
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return FACT_COUNT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(fact-count)\n"
                + "Function description:\n"
                + "\tPrint out the number of facts in the working memory.";
    }
}
