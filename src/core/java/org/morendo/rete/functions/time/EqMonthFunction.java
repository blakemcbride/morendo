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
import java.time.ZonedDateTime;

/**
 * Function will compare two dates down to the minute. That means it will lop off the seconds and
 * milliseconds. This makes it handy for time comparisons that don't need full millisecond
 * precision. An example would be to group facts by minute.
 *
 * @author Peter Lin
 */
public class EqMonthFunction extends AbstractTimeFunction implements Function {

    /** */
    public static final String EQ_MONTH = "eq-month";

    public EqMonthFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean eval = Boolean.FALSE;
        if (params != null && params.length == 2) {
            Instant date1 = this.toInstant(params[0].getValue(engine, ValueType.OBJECT));
            Instant date2 = this.toInstant(params[1].getValue(engine, ValueType.OBJECT));
            if (date1 != null && date2 != null) {
                ZonedDateTime zoned1 = zoned(date1);
                ZonedDateTime zoned2 = zoned(date2);
                if (zoned1.getYear() == zoned2.getYear()
                        && zoned1.getMonthValue() == zoned2.getMonthValue()) {
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
        return EQ_MONTH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Instant.class, Instant.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(eq-month <date> <date>)";
    }
}
