package org.morendo.rete.functions.list;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.Set;

/**
 * Function splits the string by space and then eagerly checks if part of the string matches any
 * entries in the Set. This is a type of fuzzy matching where user response doesn't have to match
 * the keys exactly.
 *
 * @author peter
 */
public class EagerSetContainsFunction implements Function {

    /** */
    public static final String MAPCONTAINS = "eager-set-contains";

    public EagerSetContainsFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        Object rl = null;
        String key = null;
        Boolean contain = Boolean.FALSE;
        if (params != null && params.length == 2) {
            Set<?> map = null;
            rl = params[0].getValue();
            key = params[1].getStringValue().toLowerCase();
            if (rl instanceof Set) {
                map = (Set<?>) rl;
            }
            if (map != null && key != null) {
                String[] tokens = key.split(" ");
                String teststr = "";
                for (int i = 0; i < tokens.length; i++) {
                    teststr += " " + tokens[i];
                    contain = map.contains(teststr.trim());
                    if (contain) {
                        break;
                    }
                }
            }
        }
        DefaultReturnValue val = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, contain);
        rv.addReturnValue(val);
        return rv;
    }

    public String getName() {
        return MAPCONTAINS;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(eager-set-contains <set> <string>)";
    }
}
