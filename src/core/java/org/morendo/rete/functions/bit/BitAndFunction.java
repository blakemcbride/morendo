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

public class BitAndFunction implements Function {

    /** */
    public static final String BIT_AND = "bit-and";

    public BitAndFunction() {}

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector returnVector = new DefaultReturnVector();
        int value = 0;
        if (params != null) {
            if (params[0] instanceof ValueParam) {
                value = ((ValueParam) params[0]).getIntValue();
            } else if (params[0] instanceof BoundParam) {
                Object v = ((BoundParam) params[0]).getValue(engine, ValueType.OBJECT);
                if (v instanceof Number number) {
                    value = (number).intValue();
                }
            }
            // iterate over the parameters
            for (int i = 1; i < params.length; i++) {
                int intval = 0;
                if (params[i] instanceof ValueParam) {
                    intval = ((ValueParam) params[i]).getIntValue();
                } else if (params[i] instanceof BoundParam) {
                    Object v = ((BoundParam) params[0]).getValue(engine, ValueType.OBJECT);
                    if (v instanceof Number numberValue) {
                        intval = (numberValue).intValue();
                    }
                }
                value = value & intval;
            }
            DefaultReturnValue returnVal =
                    new DefaultReturnValue(ValueType.INTEGER_OBJECT, Integer.valueOf(value));
            returnVector.addReturnValue(returnVal);
        }
        return returnVector;
    }

    public String getName() {
        return BIT_AND;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(" + BIT_AND + " <int>*)";
    }
}
