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
