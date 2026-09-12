package org.jamocha.rete.functions.list;

import java.util.HashSet;
import java.util.Set;

import org.jamocha.rete.BoundParam;
import org.jamocha.rete.Constants;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

/**
 * Function for creating a Java Set from strings
 * @author Peter Lin
 *
 */
public class CreateSetFunction implements Function {

	/**
	 * 
	 */
	public static final String CREATE_SET = "create-set$";
	
	public CreateSetFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		DefaultReturnVector ret = new DefaultReturnVector();
		Set<String> stringset = new HashSet<>();
		if (params != null && params.length > 0) {	
			for (int idx=0; idx < params.length; idx++) {
				if (params[idx] instanceof BoundParam) {
					BoundParam bp = (BoundParam)params[idx];
					bp.resolveBinding(engine);
				} 
				if (params[idx] instanceof ValueParam) {
					stringset.add(params[idx].getStringValue());
				} else {
					Object list = params[idx].getValue(engine, ValueType.ARRAY); 
					if (list.getClass().isArray()) {
						Object[] vals = (Object[])list;
						for (Object val : vals) {
							stringset.add(val.toString());
						}
					} else stringset.add(list.toString());
				}
			}
			DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT,
					stringset);
			ret.addReturnValue(rv);
			
		}
		return ret;
	}

	public String getName() {
		return CREATE_SET;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{ValueParam[].class};
	}

	public ValueType getReturnType() {
		return ValueType.OBJECT;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(create-set$ <value>+)";
	}

}
