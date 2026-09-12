package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/** (progn <expression>*): evaluates its arguments in order and returns the last value. */
public class PrognFunction implements Function {

    public static final String PROGN = "progn";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object last = params == null ? null : Control.runBody(engine, params, 0);
        return Control.result(last);
    }

    public String getName() {
        return PROGN;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(progn <expression>*)";
    }
}
