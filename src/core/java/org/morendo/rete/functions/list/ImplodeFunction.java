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

/**
 * Function for creating a string from a multifield. This extends the CLIPS function of the same
 * name by taking multiple multifields as parameters as opposed to a single string.
 *
 * @author Dave Woodman
 */
public class ImplodeFunction implements Function {

    /** Creates a String from a multifield */
    public static final String IMPLODE = "implode$";

    public ImplodeFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        String retStr = new String();
        Object list = null;
        for (int idx = 0; idx < params.length; idx++) {
            if (params[idx] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[idx];
                bp.resolveBinding(engine);
            }
            if (params[idx] instanceof ValueParam) {
                list = params[idx].getValue();
            } else {
                list = params[idx].getValue(engine, ValueType.ARRAY);
            }
            if (list.getClass().isArray()) {
                Object[] r = (Object[]) list;
                for (int indx = 0; indx < r.length; indx++) {
                    retStr = retStr.concat(r[indx].toString().trim().concat(" "));
                }
            } else {
                retStr = retStr.concat(list.toString().trim().concat(" "));
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.STRING, retStr.trim());

        ret.addReturnValue(rv);

        return ret;
    }

    public String getName() {
        return IMPLODE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(implode$ <multifield>+)";
    }
}
