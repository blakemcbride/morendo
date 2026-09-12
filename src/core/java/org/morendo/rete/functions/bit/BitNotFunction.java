package org.morendo.rete.functions.bit;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class BitNotFunction implements Function {

    /** */
    public static final String BIT_NOT = "bit-not";

    public BitNotFunction() {}

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector returnVector = new DefaultReturnVector();
        int value = 0;
        if (params != null && params.length == 2) {
            if (params[0] instanceof ValueParam) {
                value = ((ValueParam) params[0]).getIntValue();
            } else if (params[0] instanceof BoundParam) {
                Object v = ((BoundParam) params[0]).getValue(engine, ValueType.OBJECT);
                if (v instanceof Number number) {
                    value = (number).intValue();
                }
            }
            value = ~value;
        }
        DefaultReturnValue returnValue =
                new DefaultReturnValue(ValueType.INTEGER_OBJECT, Integer.valueOf(value));
        returnVector.addReturnValue(returnValue);
        return returnVector;
    }

    public String getName() {
        return BIT_NOT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return null;
    }
}
