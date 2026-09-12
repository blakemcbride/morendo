package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * (return [<value>]): leaves the enclosing deffunction with the value, or ends the actions of the
 * rule that is firing.
 */
public class ReturnFunction implements Function {

    public static final String RETURN = "return";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object value = params != null && params.length > 0 ? Control.eval(engine, params[0]) : null;
        throw new ControlFlow(ControlFlow.Kind.RETURN, value);
    }

    public String getName() {
        return RETURN;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(return [<value>])";
    }
}
