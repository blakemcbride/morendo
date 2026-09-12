package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * (loop-for-count (<var> [<start>] <end>) [do] <action>*) and (loop-for-count <end> <action>*). The
 * counter runs from start (default 1) to end inclusive and is bound to <var> for the body.
 */
public class LoopForCountFunction implements Function {

    public static final String LOOP_FOR_COUNT = "loop-for-count";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length > 0) {
            String variable = null;
            long start = 1;
            long end;
            // (?i 1 10) arrives as the call "bind" with the variable and the bounds as arguments
            if (params[0] instanceof FunctionParam2 spec && "bind".equals(spec.getFunctionName())) {
                Parameter[] range = spec.getParameters();
                variable = Control.variableName(range[0]);
                if (range.length >= 3) {
                    start = Control.toLong(Control.eval(engine, range[1]));
                    end = Control.toLong(Control.eval(engine, range[2]));
                } else {
                    end = Control.toLong(Control.eval(engine, range[1]));
                }
            } else {
                end = Control.toLong(Control.eval(engine, params[0]));
            }
            int from = params.length > 1 && Control.isKeyword(params[1], "do") ? 2 : 1;
            long turns = 0;
            for (long i = start; i <= end; i++) {
                Control.checkLimit(++turns);
                if (variable != null) {
                    engine.setBindingValue(variable, Control.number(i));
                }
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
        return LOOP_FOR_COUNT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(loop-for-count (<var> [<start>] <end>) [do] <action>*)";
    }
}
