package org.morendo.rete.functions.string;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/** The CLIPS str-cat function: concatenates the string form of its arguments. */
public class StrCatFunction implements Function {

    public static final String STR_CAT = "str-cat";

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        StringBuilder buf = new StringBuilder();
        if (params != null) {
            for (Parameter param : params) {
                if (param instanceof BoundParam bp) {
                    bp.resolveBinding(engine);
                }
                Object value = param.getValue(engine, ValueType.STRING);
                if (value != null) {
                    buf.append(value);
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.STRING, buf.toString()));
        return ret;
    }

    public String getName() {
        return STR_CAT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(str-cat <value>+)";
    }
}
