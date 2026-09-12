package org.morendo.rete.functions.analysis;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rule.Defrule;
import org.morendo.rule.util.PartialMatchCalculation;

public class PartialMatchCostFunction implements Function {

    /** */
    public static final String PARTIAL_MATCH_COST = "partial-match-cost";

    private PartialMatchCalculation calculation = new PartialMatchCalculation();

    public PartialMatchCostFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean success = Boolean.FALSE;
        if (params != null && params.length > 0) {
            String rulename = params[0].getStringValue();
            Defrule rule = (Defrule) engine.getCurrentFocus().findRule(rulename);
            calculation.calculatePartialMatchCost(engine, rule);
            success = Boolean.TRUE;
        }
        DefaultReturnVector returnVector = new DefaultReturnVector();
        DefaultReturnValue returnVal = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, success);
        returnVector.addReturnValue(returnVal);
        return returnVector;
    }

    public String getName() {
        return PARTIAL_MATCH_COST;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(partial-match-cost <rulename>)";
    }
}
