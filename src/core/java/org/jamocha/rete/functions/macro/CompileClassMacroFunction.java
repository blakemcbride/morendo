package org.jamocha.rete.functions.macro;

import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueParam;
import org.jamocha.rete.ValueType;

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
