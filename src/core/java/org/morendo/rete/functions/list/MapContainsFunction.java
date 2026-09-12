package org.morendo.rete.functions.list;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.Map;

/**
 * Function will test if a string is in a Map
 *
 * @author peter
 */
public class MapContainsFunction implements Function {

    /** */
    public static final String MAPCONTAINS = "map-contains";

    public MapContainsFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        Object rl = null;
        String key = null;
        Boolean contain = Boolean.FALSE;
        if (params != null && params.length == 2) {
            Map<?, ?> map = null;
            rl = params[0].getValue();
            key = params[1].getStringValue();
            if (rl instanceof Map) {
                map = (Map<?, ?>) rl;
            }
            if (map != null && key != null) {
                contain = map.containsKey(key);
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
        return "(map-contains <map> <string>)";
    }
}
