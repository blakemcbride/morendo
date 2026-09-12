package org.morendo.rete.functions.cube;

import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class ListCubesFunction implements Function {

    /** */
    public static final String LIST_DEFCUBES = "list-defcubes";

    public static final String CUBES = "cubes";

    public ListCubesFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        for (String cubeName : engine.getCubes()) {
            engine.writeMessage(cubeName + Constants.LINEBREAK, "t");
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        return ret;
    }

    public String getName() {
        return LIST_DEFCUBES;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(list-defcubes)";
    }
}
