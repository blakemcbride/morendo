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
import java.util.List;

public class TopFunction implements Function {

    /** */
    public static final String TOP = "top";

    public TopFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        Object rl = null;
        if (params != null && params.length == 2) {
            int count = params[0].getBigIntegerValue().intValue();
            rl = params[1].getValue(engine, ValueType.OBJECT);
            if (rl instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) rl;
                if (list.size() > count) {
                    List<Object> newlist = new ArrayList<>();
                    for (int idx = 0; idx < count; idx++) {
                        newlist.add(list.get(idx));
                    }
                    rl = newlist;
                }
            } else if (rl.getClass().isArray()) {
                Object[] list = (Object[]) rl;
                if (list.length > count) {
                    Object[] newlist = new Object[count];
                    for (int idx = 0; idx < count; idx++) {
                        newlist[idx] = list[idx];
                    }
                    rl = newlist;
                }
            }
        }
        DefaultReturnValue val = new DefaultReturnValue(ValueType.LIST, rl);
        rv.addReturnValue(val);
        return rv;
    }

    public String getName() {
        return TOP;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.LIST;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(top <number> <list>)";
    }
}
