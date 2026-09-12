/** */
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Woodman - 9th May 2021
 *     <p>Implements CLIPS rest$ function
 *     <p>Returns all but fist value from a multifield value
 */
public class RestFunction implements Function {

    public static final String REST = "rest$";

    public RestFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object val = new Object[0];
        List<Object> rlist = new ArrayList<>();
        if (params != null && params.length == 1) {
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }
            Object list = null;
            if (params[0] instanceof ValueParam) {
                list = ((ValueParam) params[1]).getValue();
            } else {
                list = params[0].getValue(engine, ValueType.OBJECT);
                if (list == null) // Oh, Let's try an array instead...
                    // Could also do this... list = (Object)params[0].getValue();
                    list = params[0].getValue(engine, ValueType.ARRAY);
            }
            if (list.getClass().isArray()) {
                Object[] lval = (Object[]) list;

                if (lval.length > 1) {
                    for (int indx = 1; indx < lval.length; indx++) {
                        rlist.add(lval[indx]);
                    }
                }
            }
        }
        val = rlist.toArray();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.ARRAY, val);

        ret.addReturnValue(rv);

        return ret;
    }

    public ValueType getReturnType() {
        return ValueType.ARRAY;
    }

    public String getName() {
        return REST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(rest$ <list>)";
    }
}
