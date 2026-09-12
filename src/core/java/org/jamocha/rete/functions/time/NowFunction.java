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
package org.jamocha.rete.functions.time;


import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import java.time.Instant;
import org.jamocha.rete.ValueType;


/**
 * @author Peter Lin
 * 
 * Now returns the current time as a java.time.Instant.
 */
public class NowFunction implements Function {

    /**
	 * 
	 */
	public static final String NOW = "now";

    /**
	 * 
	 */
	public NowFunction() {
		super();
	}

	public ValueType getReturnType() {
		return ValueType.LONG_OBJECT;
	}

	/**
	 * The method expects an array of ShellBoundParam. The method will use
	 * StringBuilder to resolve the binding and print out 1 binding per
	 * line.
	 */
	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		Instant now = Instant.now();
		DefaultReturnVector ret = new DefaultReturnVector();
		DefaultReturnValue rv = 
			new DefaultReturnValue(ValueType.OBJECT,now);
		ret.addReturnValue(rv);
		return ret;
	}

	public String getName() {
		return NOW;
	}

	public Class<?>[] getParameter() {
        return new Class<?>[0];
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(now)";
	}
}
