package woolfel.examples.function;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

public class HelloFunction implements Function {

    /** */
    private static final long serialVersionUID = 1L;

    public static final String HELLO = "hello";

    public HelloFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector returnVec = new DefaultReturnVector();
        if (params != null && params.length > 0) {
            for (int idx = 0; idx < params.length; idx++) {
                DefaultReturnValue value =
                        new DefaultReturnValue(ValueType.STRING, params[idx].getStringValue());
                returnVec.addReturnValue(value);
            }
        }
        return returnVec;
    }

    public String getName() {
        return HELLO;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public String toPPString(Parameter[] params, int indents) {
        return null;
    }
}
