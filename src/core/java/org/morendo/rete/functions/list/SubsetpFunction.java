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

import java.util.Objects;

public class SubsetpFunction implements Function {

    /** Checks if one multifield is a subset of the other */
    public static final String SUBSETP = "subsetp";

    public SubsetpFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object checkList = null;
        Object list2Check = null;
        Boolean result = Boolean.FALSE;
        if (params != null && params.length <= 2) {
            // first is subject to check
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }

            if (params[0] instanceof ValueParam) {
                checkList = params[0].getValue();
            } else {
                checkList = params[0].getValue(engine, ValueType.ARRAY);
            }

            // second is the list to be checked
            if (params[1] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }
            if (params[1] instanceof ValueParam) {
                list2Check = params[1].getValue();
            } else {
                list2Check = params[1].getValue(engine, ValueType.ARRAY);
            }

            if (checkList.getClass().isArray() && list2Check.getClass().isArray()) {
                Object[] cl = (Object[]) checkList;
                Object[] l2c = (Object[]) list2Check;

                result = Boolean.TRUE;
                for (Object check : cl) {
                    Boolean found = false;
                    for (Object o2Check : l2c) {
                        if (Objects.equals(check, o2Check)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) { // Missed one, set result and bail
                        result = Boolean.FALSE;
                        break;
                    }
                }
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, result);

        ret.addReturnValue(rv);

        return ret;
    }

    public String getName() {
        return SUBSETP;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(aubsetp <multifield> <multifield>)\n"
                + " checks that first multifield is a subset of the second";
    }
}
