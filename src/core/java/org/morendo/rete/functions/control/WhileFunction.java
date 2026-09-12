package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/** (while <test> [do] <action>*): runs the actions while the test is true. */
public class WhileFunction implements Function {

    public static final String WHILE = "while";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length > 0) {
            int from = params.length > 1 && Control.isKeyword(params[1], "do") ? 2 : 1;
            long turns = 0;
            while (Control.isTrue(Control.eval(engine, params[0]))) {
                Control.checkLimit(++turns);
                try {
                    Control.runBody(engine, params, from);
                } catch (ControlFlow flow) {
                    if (flow.isReturn()) {
                        throw flow;
                    }
                    break;
                }
            }
        }
        return Control.result(Boolean.FALSE);
    }

    public String getName() {
        return WHILE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(while <test> [do] <action>*)";
    }
}
