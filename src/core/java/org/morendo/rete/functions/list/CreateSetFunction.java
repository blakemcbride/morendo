package org.morendo.rete.functions.list;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.HashSet;
import java.util.Set;

/**
 * Function for creating a Java Set from strings
 *
 * @author Peter Lin
 */
public class CreateSetFunction implements Function {

    /** */
    public static final String CREATE_SET = "create-set$";

    public CreateSetFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Set<String> stringset = new HashSet<>();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    bp.resolveBinding(engine);
                }
                if (params[idx] instanceof ValueParam) {
                    stringset.add(params[idx].getStringValue());
                } else {
                    Object list = params[idx].getValue(engine, ValueType.ARRAY);
                    if (list.getClass().isArray()) {
                        Object[] vals = (Object[]) list;
                        for (Object val : vals) {
                            stringset.add(val.toString());
                        }
                    } else stringset.add(list.toString());
                }
            }
            DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT, stringset);
            ret.addReturnValue(rv);
        }
        return ret;
    }

    public String getName() {
        return CREATE_SET;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(create-set$ <value>+)";
    }
}
