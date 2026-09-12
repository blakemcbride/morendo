package org.morendo.rete.functions.temporal;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.Template;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;

public class SetTemporalDistanceFunction implements Function {

    /** */
    public static final String SET_TEMPORAL_DISTANCE = "set-temporal-distance";

    public SetTemporalDistanceFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length == 2) {
            String template = params[0].getStringValue();
            BigDecimal sec = params[1].getBigDecimalValue();
            Template templ = engine.findTemplate(template);
            if (templ != null) {
                templ.setTemporalDistance(sec.intValue() * 1000);
                DefaultReturnValue ret = new DefaultReturnValue(ValueType.BIG_DECIMAL, sec);
                rv.addReturnValue(ret);
            }
        }
        return rv;
    }

    public String getName() {
        return SET_TEMPORAL_DISTANCE;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class, ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(set-temporal-distance <deftemplate> <seconds>)";
    }
}
