package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class OrFunction implements Function {

    /** */
    public static final String OR = "or";

    public OrFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        Boolean eq = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                if (eq) {
                    break;
                } else {
                    // or expects nested functions
                    if (params[idx] instanceof FunctionParam2) {
                        FunctionParam2 n = (FunctionParam2) params[idx];
                        n.setEngine(engine);
                        n.lookUpFunction();
                        ReturnVector rval = (ReturnVector) n.getValue();
                        eq = rval.firstReturnValue().getBooleanValue();
                    }
                }
            }
        }
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, eq);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return OR;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(or <expression>)";
    }
}
