package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/** (foreach <var> <list> <action>*): binds <var> to each element in turn and runs the actions. */
public class ForeachFunction implements Function {

    public static final String FOREACH = "foreach";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length >= 2) {
            String variable = Control.variableName(params[0]);
            Object[] elements = Control.elements(Control.eval(engine, params[1]));
            long index = 0;
            for (Object element : elements) {
                engine.setBindingValue(variable, element);
                engine.setBindingValue(variable + "-index", Control.number(++index));
                try {
                    Control.runBody(engine, params, 2);
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
        return FOREACH;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(foreach <var> <list> <action>*)";
    }
}
