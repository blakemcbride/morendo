package org.morendo.rete.functions.query;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class QueryTimeFunction implements Function {

    /** */
    public static final String QUERY_TIME = "query-time";

    public QueryTimeFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        long time = 0;
        if (params != null && params.length > 0) {
            String name = params[0].getStringValue();
            time = engine.getQueryTime(name);
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.LONG_OBJECT, Long.valueOf(time));
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return QUERY_TIME;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public ValueType getReturnType() {
        return ValueType.LONG_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(query-time <query name>)";
    }
}
