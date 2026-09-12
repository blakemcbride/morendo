package org.morendo.rete.functions;

import org.morendo.rete.BoundParam;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class IsNotNilFunction implements Function {

    public static final String ISNOTNIL = "is-not-nil";

    public IsNotNilFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean eq = Boolean.TRUE;
        Boolean err = Boolean.FALSE;
        Object val = null;
        int index = 0;
        BoundParam bp = null;
        DefaultReturnValue rv;

        if (params != null && params.length > 0) {
            for (index = 0; index < params.length; index++) {
                bp = (BoundParam) params[index];
                val = bp.getValue();
                if (val == null) {
                    eq = Boolean.FALSE;
                    val = engine.getBinding(bp.getVariableName());
                }
                if (val == null) {
                    err = Boolean.TRUE;
                    break;
                }
                if (val.equals(Constants.NIL_SYMBOL)) {
                    eq = Boolean.FALSE;
                    break;
                }
            }
        } else err = Boolean.TRUE;

        DefaultReturnVector ret = new DefaultReturnVector();
        rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eq);
        ret.addReturnValue(rv);
        if (err) {
            rv =
                    new DefaultReturnValue(
                            ValueType.STRING, "Parameter error: " + bp.getVariableName());
            ret.addReturnValue(rv);
        }
        return ret;
    }

    public String getName() {
        return ISNOTNIL;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(is-not-nil <binding>)+\n"
                + "Function description:\n"
                + "\tCompares the bindings against 'nil'\n"
                + "\treturns true if all bindings are not nil, false if one or more are nil"
                + " or in the event of a variable not being bound.";
    }
}
