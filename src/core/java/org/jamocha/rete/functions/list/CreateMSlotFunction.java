package org.jamocha.rete.functions.list;

import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Function for creating a multislot with 1 or more literals
 *
 * @author Peter Lin
 */
public class CreateMSlotFunction implements Function {

    /** */
    public static final String CREATE_MULTISLOT = "create$";

    public CreateMSlotFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        List<Object> list = new ArrayList<>();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof ValueParam) {
                    list.add(params[idx].getValue());
                } else {
                    Object val = params[idx].getValue(engine, ValueType.OBJECT);
                    list.add(val);
                }
            }
            if (params[0].getValue() instanceof String) {
                String[] mval = new String[params.length];
                mval = list.toArray(mval);
                DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, mval);
                ret.addReturnValue(rv);
            } else {
                Object[] value = list.toArray();
                DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, value);
                ret.addReturnValue(rv);
            }
        } else {
            Object[] value = list.toArray();
            DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, value);
            ret.addReturnValue(rv);
        }
        return ret;
    }

    public String getName() {
        return CREATE_MULTISLOT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(create$ <value>+)";
    }
}
