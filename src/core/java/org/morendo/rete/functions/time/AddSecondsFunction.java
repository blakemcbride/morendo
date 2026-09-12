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
package org.morendo.rete.functions.time;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AddSecondsFunction extends AbstractTimeFunction implements Function {

    /** */
    public static final String ADD_SECONDS = "add-seconds";

    public AddSecondsFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Instant date = null;
        if (params != null && params.length == 2) {
            int seconds = params[0].getIntValue();
            date = this.toInstant(params[1].getValue(engine, ValueType.OBJECT));
            if (date != null) {
                date = date.plus(seconds, ChronoUnit.SECONDS);
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.DATE, date);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return ADD_SECONDS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Instant.class};
    }

    public ValueType getReturnType() {
        return ValueType.DATE;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(add-seconds <seconds> <date>)";
    }
}
