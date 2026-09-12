package org.morendo.rete.functions.query;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.Query;

import java.util.ArrayList;
import java.util.List;

public class RunQueryFunction implements Function {

    /** */
    public static final String RUN_QUERY = "run-query";

    public RunQueryFunction() {}

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        List<?> results = new ArrayList<>();
        if (params.length > 0) {
            String name = params[0].getStringValue();
            Parameter[] queryParams = new Parameter[params.length - 1];
            System.arraycopy(params, 1, queryParams, 0, queryParams.length);
            Query query = engine.getDefquery(name);
            results = query.executeQuery(engine, engine.getWorkingMemory(), queryParams);
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.OBJECT, results);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return RUN_QUERY;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String[].class};
    }

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(run-query <query name> <parametes>)";
    }
}
