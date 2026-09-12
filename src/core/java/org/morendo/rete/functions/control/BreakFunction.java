package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/** (break): leaves the innermost loop. */
public class BreakFunction implements Function {

    public static final String BREAK = "break";

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        throw new ControlFlow(ControlFlow.Kind.BREAK, null);
    }

    public String getName() {
        return BREAK;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(break)";
    }
}
