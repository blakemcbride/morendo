package org.jamocha.rete.functions.query;

import org.jamocha.rete.DefaultReturnValue;
import org.jamocha.rete.DefaultReturnVector;
import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;
import org.jamocha.rule.Query;

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
