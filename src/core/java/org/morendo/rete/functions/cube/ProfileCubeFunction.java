package org.morendo.rete.functions.cube;

import org.morendo.rete.Cube;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

public class ProfileCubeFunction implements Function {

    /** */
    public static final String PROFILE_CUBE = "profile-cube";

    public ProfileCubeFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean profile = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                String cubename = params[idx].getStringValue();
                Cube c = engine.getCube(cubename);
                if (c != null) {
                    c.setProfileQuery(true);
                    profile = Boolean.TRUE;
                }
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        DefaultReturnValue rv = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, profile);
        ret.addReturnValue(rv);
        return ret;
    }

    public String getName() {
        return PROFILE_CUBE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(profile-cube <name>)";
    }
}
