package org.morendo.rete.functions.query;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.Query;

public class UnWatchQueryFunction implements Function {

    /** */
    public static final String WATCH_QUERY = "unwatch-query";

    public UnWatchQueryFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Boolean watch = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                String name = params[i].getStringValue();
                Query query = engine.getDefquery(name);
                if (query != null) {
                    query.setWatch(false);
                }
            }
            watch = Boolean.TRUE;
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, watch);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return WATCH_QUERY;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class, String.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(unwatch-query <query name>)";
    }
}
