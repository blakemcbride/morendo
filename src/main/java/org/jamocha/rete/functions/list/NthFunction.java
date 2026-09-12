package org.jamocha.rete.functions.list;

import java.math.BigDecimal;

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
 * Nth function will return the value at the given index
 * @author Peter Lin
 *
 */
public class NthFunction implements Function {

	/**
	 * 
	 */
	public static final String NTH = "nth$";
	
	public NthFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		DefaultReturnVector ret = new DefaultReturnVector();
		Object val = null;
		if (params != null && params.length == 2) {
			if (params[0] instanceof BoundParam) {
				BoundParam bp = (BoundParam)params[0];
				bp.resolveBinding(engine);
			}
			if (params[1] instanceof BoundParam) {
				BoundParam bp = (BoundParam)params[1];
				bp.resolveBinding(engine);
			}
			int index = 0;
			Object list = null;
			if (params[0] instanceof ValueParam) {
				index = ((ValueParam)params[0]).getIntValue();
			} else {
				BigDecimal bval = new BigDecimal(params[0].getValue(engine, ValueType.BIG_DECIMAL).toString());
				index = bval.intValue();
			}
			if (params[1] instanceof ValueParam) {
				list = ((ValueParam)params[1]).getValue();
			} else {
				list = params[1].getValue(engine, ValueType.OBJECT);
				if (list == null )  // Oh, Let's try an array instead...
					// Could also do this... list = (Object)params[1].getValue();
					list = params[1].getValue(engine, ValueType.ARRAY);
			}
			index--; // Compensate for zero-based in Java vs 1 based in CLIPS
			if (list.getClass().isArray()) {
				Object[] lval = (Object[])list;
				if (index >= lval.length) {
					val = Boolean.FALSE;
				} else {
					val = lval[index];
				}
			}
		}
		DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT,
				val);
		ret.addReturnValue(rv);
		return ret;
	}

	public String getName() {
		return NTH;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{ValueParam[].class};
	}

	public ValueType getReturnType() {
		return ValueType.INTEGER_OBJECT;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(nth$ <single-value> <list>)";
	}

}
