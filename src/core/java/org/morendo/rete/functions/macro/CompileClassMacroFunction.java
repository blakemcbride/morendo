package org.morendo.rete.functions.macro;

import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

/**
 * TODO Need to finish this function one day
 *
 * @author peter
 */
public class CompileClassMacroFunction implements Function {

    /** */
    public static final String COMPILE_MACRO = "compile-class-macro";

    public CompileClassMacroFunction() {
        super();
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        return null;
    }

    public String getName() {
        return COMPILE_MACRO;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {ValueParam.class};
    }

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(compile-class-macro <classname>)";
    }
}
