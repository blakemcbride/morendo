package org.jamocha.rete.functions.list;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
 * @author Peter Lin
 *
 */
public class DeleteRangeFunction implements Function {

	/**
	 * 
	 */
	public static final String DELETE_RANGE = "delete$";
	
	public DeleteRangeFunction() {
		super();
	}

	public ReturnVector executeFunction(Rete engine, Parameter[] params) {
		DefaultReturnVector ret = new DefaultReturnVector();
		Object value = new Object[0];
		if (params != null && params.length > 0) {
			List<Object> rlist = new ArrayList<>();
			Object list = null;
			if (params[0] instanceof ValueParam) {
				list = params[0].getValue();
			} else {
				list = params[0].getValue(engine, ValueType.ARRAY);
			}
			int startIndex = 0;
			if (params[1] instanceof ValueParam) {
				startIndex = params[1].getBigDecimalValue().intValue() ;
			} else if (params[1] instanceof BoundParam) {
				Object bval = ((BoundParam)params[1]).getValue(engine, ValueType.INT_PRIM);
				startIndex = ((BigDecimal)bval).intValue();
			}
			int endIndex = 0;
			if (params[2] instanceof ValueParam) {
				endIndex = params[2].getBigDecimalValue().intValue();
			} else if (params[2] instanceof BoundParam) {
				Object bval = ((BoundParam)params[2]).getValue(engine, ValueType.INT_PRIM);
				endIndex = ((BigDecimal)bval).intValue();
			}
			//Make 1 bases
			startIndex--;
			endIndex--; 
			if (list.getClass().isArray()) {
				Object[] r = (Object[])list;
				if (r.length >= startIndex) {
					for (int idx=0; idx < startIndex; idx++) {
						rlist.add(r[idx]);
					}
					if (r.length > endIndex) {
						for (int idz=endIndex+1; idz < r.length; idz++) {
							rlist.add(r[idz]);
						}
					}
				} else {
					for (int idx=0; idx < r.length; idx++) {
						rlist.add(r[idx]);
					}
				}
			}
			value = rlist.toArray();
		}
		DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY,
				value);
		ret.addReturnValue(rv);
		return ret;
	}

	public String getName() {
		return DELETE_RANGE;
	}

	public Class<?>[] getParameter() {
		return new Class<?>[]{ValueParam[].class};
	}

	public ValueType getReturnType() {
		return ValueType.ARRAY;
	}

	public String toPPString(Parameter[] params, int indents) {
		return "(delete$ <list> <begin-index> <end-index>)";
	}

}
