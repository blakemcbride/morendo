/*
 * Copyright 2026 Blake McBride
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

/** {@code (fact-existp <fact>)}: true while the fact is in working memory. */
public class FactExistpFunction implements Function {

    public static final String FACT_EXISTP = "fact-existp";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        boolean exists = false;
        if (params != null && params.length == 1) {
            org.morendo.rete.Fact fact = FactLookup.find(engine, params[0]);
            exists = fact != null && engine.getFactById(fact.getFactId()) != null;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, exists));
        return ret;
    }

    public String getName() {
        return FACT_EXISTP;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(fact-existp <fact> | <id>)\n true if the fact is in working memory.";
    }
}
