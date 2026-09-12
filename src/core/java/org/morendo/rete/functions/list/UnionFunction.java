package org.morendo.rete.functions.list;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.ArrayList;

/**
 * Simple union function that adds the items to a list and gets rid of duplicates. It uses
 * ArrayList.contains to test for duplicates.
 *
 * @author Peter Lin
 */
public class UnionFunction implements Function {

    /** */
    public static final String UNION = "union$";

    public UnionFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object[] value = null;
        ArrayList<Object> list = new ArrayList<>();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                Object v = params[idx].getValue(engine, ValueType.OBJECT);
                if (v.getClass().isArray()) {
                    Object[] array = (Object[]) v;
                    for (int idz = 0; idz < array.length; idz++) {
                        if (!list.contains(array[idz])) {
                            list.add(array[idz]);
                        }
                    }
                }
            }
        }
        value = new Object[list.size()];
        value = list.toArray(value);
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, value);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return UNION;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(union$ <list> <list>)";
    }
}
