package org.morendo.rete.functions.list;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.List;

public class MemberTestFunction implements Function {

    /** */
    public static final String MEMBER_TEST = "member$";

    public MemberTestFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        // int index = -1; Unsued
        Boolean member = Boolean.FALSE;
        if (params != null && params.length == 2) {
            Object item = params[0].getValue();
            Object l = params[1];
            if (l instanceof ValueParam valueParam) {
                Object list = (valueParam).getValue();
                if (list.getClass().isArray()) {
                    Object[] ary = (Object[]) list;
                    for (int idx = 0; idx < ary.length; idx++) {
                        if (ary[idx].equals(item)) {
                            // index = idx;
                            member = Boolean.TRUE;
                            break;
                        }
                    }
                }
            } else {
                Object list = params[1].getValue(engine, ValueType.OBJECT);
                if (list.getClass().isArray()) {
                    Object[] ary = (Object[]) list;
                    for (int idx = 0; idx < ary.length; idx++) {
                        if (ary[idx].equals(item)) {
                            // index = idx;
                            member = Boolean.TRUE;
                            break;
                        }
                    }
                } else if (list instanceof List) {
                    List<?> alist = (List<?>) list;
                    member = alist.contains(item);
                }
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, member);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return MEMBER_TEST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(member$ <single> <list>)";
    }
}
