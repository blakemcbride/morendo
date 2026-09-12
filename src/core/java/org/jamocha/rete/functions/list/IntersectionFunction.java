package org.jamocha.rete.functions.list;

import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Lin
 */
public class IntersectionFunction implements Function {

    /** */
    public static final String INTERSECTION = "intersection$";

    public IntersectionFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object[] result = null;
        if (params != null && params.length == 2) {
            Object array1 = params[0].getValue(engine, ValueType.OBJECT);
            Object array2 = params[1].getValue(engine, ValueType.OBJECT);
            if (array1 instanceof Object[] && array2 instanceof Object[]) {
                Set<Object> set1 = convertToList((Object[]) array1);
                Set<Object> set2 = convertToList((Object[]) array2);
                set1.retainAll(set2);
                result = set1.toArray();
            }
        }

        DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, result);
        ret.addReturnValue(rv);
        return ret;
    }

    protected Set<Object> convertToList(Object[] array) {
        Set<Object> data = new HashSet<>();
        for (int idx = 0; idx < array.length; idx++) {
            data.add(array[idx]);
        }
        return data;
    }

    public String getName() {
        return INTERSECTION;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(intersection$ <list> <list>)";
    }
}
