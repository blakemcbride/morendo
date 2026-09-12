package org.morendo.rete.functions.macro;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * TODO need to finish this function one day
 *
 * @author peter
 */
public class JarClassMacroFunction implements Function {

    /** */
    public static final String JAR_MACRO = "jar-class-macro";

    public JarClassMacroFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        return null;
    }

    public String getName() {
        return JAR_MACRO;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(jar-class-macro <classname>)";
    }
}
