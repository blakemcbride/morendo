package org.morendo.rete.functions.string;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class StringContainsFunction implements Function {

    /** */
    public static final String STRING_CONTAINS = "str-contains";

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean contain = Boolean.FALSE;
        if (params != null && params.length == 2) {
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }
            if (params[1] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[1];
                bp.resolveBinding(engine);
            }
            String val = params[0].getStringValue();
            String pt = params[1].getStringValue();
            contain = val.contains(pt);
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, contain);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return STRING_CONTAINS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(str-contains [string] [pattern])";
    }
}
