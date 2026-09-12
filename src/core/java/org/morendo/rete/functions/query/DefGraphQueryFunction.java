package org.morendo.rete.functions.query;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rule.GraphQuery;

public class DefGraphQueryFunction implements Function {

    /** */
    public static final String DEFQUERY = "defgraphquery";

    public DefGraphQueryFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean add = Boolean.TRUE;
        if (params.length == 1 && params[0].getValue() instanceof GraphQuery) {
            GraphQuery query = (GraphQuery) params[0].getValue();
            add = engine.getGraphQueryCompiler().addQuery(query);
        } else {
            add = Boolean.FALSE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, add);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return DEFQUERY;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        if (params != null) {
            StringBuilder buf = new StringBuilder();
            return buf.toString();
        } else {
            return "(defgraphquery <query-name> (declare (variables <binding>)+) (CE)+ )"
                    + ""
                    + "(defgraphquery <query-name> \"optional_comment\" "
                    + "	(variables ) 		; inputs for the query (LHS)"
                    + "	(pattern_1) 		; Left-Hand Side (LHS)"
                    + "	(pattern_2) 		; of the rule consisting of elements"
                    + "	...					; before the \"=>\""
                    + "	...					"
                    + "	...					"
                    + "	(pattern_N)"
                    + ""
                    + "Be sure all your parentheses balance or you will get error messages!";
        }
    }
}
