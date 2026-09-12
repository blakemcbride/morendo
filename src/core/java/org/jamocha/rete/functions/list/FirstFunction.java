/** */
package org.jamocha.rete.functions.list;

import org.jamocha.rete.BoundParam;
import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

/**
 * @author Dave Woodman 9 May 2021
 *     <p>Implements CLIPS multislot function first$
 *     <p>Returns first value from a multifield
 */
public class FirstFunction implements Function {

    public static final String FIRST = "first$";

    public FirstFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Object val = null;
        if (params != null && params.length == 1) {
            if (params[0] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[0];
                bp.resolveBinding(engine);
            }
            Object list = null;

            if (params[0] instanceof ValueParam) {
                list = ((ValueParam) params[0]).getValue();
            } else {
                list = params[0].getValue(engine, ValueType.OBJECT);
                if (list == null) // Oh, Let's try an array instead...
                    // Could also do this... list = (Object)params[0].getValue();
                    list = params[0].getValue(engine, ValueType.ARRAY);
            }
            if (list.getClass().isArray()) {
                Object[] lval = (Object[]) list;
                if (lval.length > 0) val = lval[0];
                else val = ""; // Should never get here!
            } else val = list; // Last item
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT, val);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return FIRST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.INTEGER_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(first$ <list>)";
    }
}
