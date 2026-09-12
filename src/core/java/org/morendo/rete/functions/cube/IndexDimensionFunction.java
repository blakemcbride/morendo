package org.morendo.rete.functions.cube;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Defcube;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class IndexDimensionFunction implements Function {

    /** */
    public static final String INDEX_DIMENSION = "index-dimension";

    public IndexDimensionFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean index = Boolean.FALSE;
        if (params != null && params.length >= 2) {
            String cubename = params[0].getStringValue();
            Defcube cube = (Defcube) engine.getCube(cubename);
            for (int i = 1; i < params.length; i++) {
                String dimension = params[i].getStringValue();
                cube.getDimension(dimension).setAutoIndex(true);
            }
            index = Boolean.TRUE;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, index);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return INDEX_DIMENSION;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class, String[].class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(index-dimension <cube> <dimension>+)";
    }
}
