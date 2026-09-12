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


import org.jamocha.rete.BoundParam;
import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import java.time.Instant;
import java.time.ZonedDateTime;
import org.jamocha.rete.ValueType;

/**
 * Function will compare two dates down to the minute. That means it will lop off the seconds
 * and milliseconds. This makes it handy for time comparisons that don't need full millisecond
 * precision. An example would be to group facts by minute.
 * 
 * @author Peter Lin
 */
public class EqHourFunction extends AbstractTimeFunction implements Function {

	/**
	 * 
	 */
	public static final String EQ_HOUR = "eq-hour";
	
	public EqHourFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		Boolean eval = Boolean.FALSE;
		if (params != null && params.length == 2) {
			Instant date1 = null;
			if (params[0] instanceof ValueParam) {
				date1 = this.toInstant(params[0].getValue());
			} else if (params[0] instanceof BoundParam) {
				date1 = this.toInstant(engine.getBinding( ((BoundParam)params[0]).getVariableName()));
			}
			Instant date2 = null;
			if (params[1] instanceof ValueParam) {
				date2 = this.toInstant(params[1].getValue());
			} else if (params[1] instanceof BoundParam) {
				date2 = this.toInstant(engine.getBinding( ((BoundParam)params[1]).getVariableName()));
			}
			if (date1 != null && date2 != null) {
				ZonedDateTime zoned1 = zoned(date1);
				ZonedDateTime zoned2 = zoned(date2);
				if (zoned1.getYear() == zoned2.getYear() &&
						zoned1.getMonthValue() == zoned2.getMonthValue() &&
						zoned1.getDayOfMonth() == zoned2.getDayOfMonth() &&
						zoned1.getHour() == zoned2.getHour()) {
					eval = Boolean.TRUE;
				}
			}
		}
		DefaultReturnVector ret = new DefaultReturnVector();
		DefaultReturnValue rv = 
			new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eval);
		ret.addReturnValue(rv);
		return ret;
	}

	public String getName() {
		return EQ_HOUR;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{Instant.class, Instant.class};
	}

	public ValueType getReturnType() {
		return ValueType.BOOLEAN_OBJECT;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(eq-hour <date> <date>)";
	}

}
