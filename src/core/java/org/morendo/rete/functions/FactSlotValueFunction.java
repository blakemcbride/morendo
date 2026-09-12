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
import org.morendo.rete.Fact;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (fact-slot-value <fact> <slot>)}: the value of a slot of a bound fact or a fact id. */
public class FactSlotValueFunction implements Function {

    public static final String FACT_SLOT_VALUE = "fact-slot-value";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Fact fact =
                params != null && params.length == 2 ? FactLookup.find(engine, params[0]) : null;
        if (fact != null) {
            String slot = String.valueOf(params[1].getValue(engine, ValueType.STRING));
            int column = fact.getDeftemplate().getColumnIndex(slot);
            if (column >= 0) {
                Object value = fact.getSlotValue(column);
                ValueType type = value instanceof Object[] ? ValueType.ARRAY : ValueType.OBJECT;
                ret.addReturnValue(new DefaultReturnValue(type, value));
                return ret;
            }
        }
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE));
        return ret;
    }

    public String getName() {
        return FACT_SLOT_VALUE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object.class, String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(fact-slot-value <fact> <slot>)\n the value of a slot; false for an unknown fact"
                + " or slot.";
    }
}
