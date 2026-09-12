package org.jamocha.rete.functions.query;

import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

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
