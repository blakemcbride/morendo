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
package org.jamocha.rete.functions.time;

import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Function will compare two dates. The second date must be within x minutes of the first date
 * value. For example if fact A has a timestamp of 2010/3/1:03:01:23 and fact B has a timestamp of
 * 2010/3/1:03:02:05 is within 1 minute.
 *
 * @author Peter Lin
 */
public class WithinMinutesFunction extends AbstractTimeFunction implements Function {

    /** */
    public static final String WITHIN_MINUTES = "within-minutes";

    public WithinMinutesFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean eval = Boolean.FALSE;
        if (params != null && params.length == 3) {
            int interval = params[0].getIntValue();
            Instant date1 = this.toInstant(params[1].getValue(engine, ValueType.OBJECT));
            Instant date2 = this.toInstant(params[2].getValue(engine, ValueType.OBJECT));
            if (date1 != null && date2 != null) {
                Instant end = date1.plus(interval, ChronoUnit.MINUTES);
                if (!date2.isBefore(date1) && !date2.isAfter(end)) {
                    eval = Boolean.TRUE;
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eval);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return WITHIN_MINUTES;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Instant.class, Instant.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(within-minutes <date> <date>)";
    }
}
