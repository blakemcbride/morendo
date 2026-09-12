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
package org.jamocha.rete.functions;


import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

/**
 * @author Peter Lin
 *
 * Reset the objects means retract all the objects and assert
 * them again.
 */
public class ResetObjectsFunction implements Function {

	/**
	 * 
	 */
	public static final String RESET_OBJECTS = "reset-objects";
	
	/**
	 * 
	 */
	public ResetObjectsFunction() {
		super();
	}

	public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		engine.resetObjects();
		return new DefaultReturnVector();
	}

	public String getName() {
		return RESET_OBJECTS;
	}


	public Class<?>[] getParameter() {
		return new Class<?>[0];
	}

	public String toPPString(Parameter[] params, int indents) {
		StringBuilder buf = new StringBuilder();
		return buf.toString();
	}
}
