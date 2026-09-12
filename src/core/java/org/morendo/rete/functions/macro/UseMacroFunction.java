package org.morendo.rete.functions.macro;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Defclass;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class UseMacroFunction implements Function {

    /** */
    public static final String USE_MACRO = "use-macro";

    public UseMacroFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Boolean use = Boolean.FALSE;
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof ValueParam) {
                    String classname = ((ValueParam) params[idx]).getStringValue();
                    Defclass defclass = engine.findDefclassByName(classname);
                    defclass.loadMacros(engine.getCurrentFocus().getModuleClassLoader());
                }
            }
            use = Boolean.TRUE;
        }
        DefaultReturnVector rv = new DefaultReturnVector();
        DefaultReturnValue rval = new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, use);
        rv.addReturnValue(rval);
        return rv;
    }

    public String getName() {
        return USE_MACRO;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(use-macro <classname>)";
    }
}
