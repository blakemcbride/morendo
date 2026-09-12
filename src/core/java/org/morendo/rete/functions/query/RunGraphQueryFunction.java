package org.morendo.rete.functions.query;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffact;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.GraphQuery;
import org.morendo.rule.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Running a graphQuery is different than a regular query in a couple of ways. The first is the
 * graph data isn't added to the main engine working memory. Second is the query can return either a
 * list of nodes or edges.
 *
 * @author peter
 */
public class RunGraphQueryFunction implements Function {

    /** */
    public static final String RUN_QUERY = "run-graph-query";

    public RunGraphQueryFunction() {}

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        List<?> results = new ArrayList<>();
        if (params.length > 0) {
            String bindname = ((BoundParam) params[0]).getVariableName();
            String name = params[1].getStringValue();
            Parameter[] queryParams = new Parameter[params.length - 2];
            System.arraycopy(params, 2, queryParams, 0, queryParams.length);
            Query query = engine.getGraphQuery(name);
            Object bval = engine.getBinding(bindname);
            Deffact[] facts = (Deffact[]) bval;
            if (facts != null && query != null) {
                ((GraphQuery) query).setGraphData(facts);
                results = query.executeQuery(engine, engine.getWorkingMemory(), queryParams);
            }
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
        return "(run-graph-query <graph data> <query name> <parametes>)";
    }
}
