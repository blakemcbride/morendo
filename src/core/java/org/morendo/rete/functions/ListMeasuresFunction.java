package org.morendo.rete.functions;

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rete.measures.Measure;

import java.util.List;

public class ListMeasuresFunction implements Function {

    /** */
    public static final String MEASURES = "measures";

    public ListMeasuresFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        List<?> measures = engine.getAllMeasures();
        int counter = 0;
        for (int idx = 0; idx < measures.size(); idx++) {
            Measure m = (Measure) measures.get(idx);
            engine.writeMessage("  " + m.getMeasureName() + Constants.LINEBREAK, "t");
            counter++;
        }
        engine.writeMessage(counter + " measures" + Constants.LINEBREAK, "t");
        return new DefaultReturnVector();
    }

    public String getName() {
        return MEASURES;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(measures)";
    }
}
