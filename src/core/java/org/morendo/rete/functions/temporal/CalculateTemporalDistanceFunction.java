package org.morendo.rete.functions.temporal;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.util.Collection;

public class CalculateTemporalDistanceFunction implements Function {

    /** */
    public static final String CALCULATE_DISTANCE = "calculate-temporal-distance";

    public CalculateTemporalDistanceFunction() {
        super();
    }

    /**
     * function isn't implemented yet. need to implement temporal distance calculation utility first
     */
    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean successful = Boolean.FALSE;
        TemporalCalculation calculation = new TemporalCalculation();
        Collection<?> rules = engine.getCurrentFocus().getAllRules();
        successful = calculation.calcuateDistance(engine, rules);
        DefaultReturnVector rv = new DefaultReturnVector();
        DefaultReturnValue value = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, successful);
        rv.addReturnValue(value);
        return rv;
    }

    public String getName() {
        return CALCULATE_DISTANCE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(calculate-temporal-distance <file> <outputfile>)";
    }
}
