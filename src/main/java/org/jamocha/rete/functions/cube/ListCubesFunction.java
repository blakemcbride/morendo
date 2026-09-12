package org.jamocha.rete.functions.cube;

import java.util.Iterator;
import java.util.List;

import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

public class ListCubesFunction implements Function {

	/**
	 * 
	 */
	public static final String LIST_DEFCUBES = "list-defcubes";
	public static final String CUBES = "cubes";
	
	public ListCubesFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		for (String cubeName : engine.getCubes()) {
			engine.writeMessage(cubeName + Constants.LINEBREAK, "t");
		}
		DefaultReturnVector ret = new DefaultReturnVector();
		return ret;
	}

	public String getName() {
		return LIST_DEFCUBES;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[0];
	}

	public ValueType getReturnType() {
		return ValueType.RETURN_VOID;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(list-defcubes)";
	}

}
