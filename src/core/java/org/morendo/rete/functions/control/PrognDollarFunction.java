package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * (progn$ (<var> <list>) <action>*): the CLIPS spelling of foreach; <var>-index holds the position
 * of the element, counting from 1.
 */
public class PrognDollarFunction implements Function {

    public static final String PROGN_DOLLAR = "progn$";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object last = null;
        if (params != null && params.length >= 1) {
            String variable;
            Object[] elements;
            // (?x <list>) arrives as the call "bind" with the variable and the list
            if (params[0] instanceof FunctionParam2 spec && "bind".equals(spec.getFunctionName())) {
                Parameter[] range = spec.getParameters();
                variable = Control.variableName(range[0]);
                elements = Control.elements(Control.eval(engine, range[1]));
            } else {
                return Control.result(null);
            }
            long index = 0;
            for (Object element : elements) {
                engine.setBindingValue(variable, element);
                engine.setBindingValue(variable + "-index", Control.number(++index));
                try {
                    last = Control.runBody(engine, params, 1);
                } catch (ControlFlow flow) {
                    if (flow.isReturn()) {
                        throw flow;
                    }
                    break;
                }
            }
        }
        return Control.result(last);
    }

    public String getName() {
        return PROGN_DOLLAR;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(progn$ (<var> <list>) <action>*)";
    }
}
