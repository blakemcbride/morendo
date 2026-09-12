package org.morendo.rete.functions.macro;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class MacroFunctions implements FunctionGroup {

    /** */
    public static final String MACRO_FUNCTIONS = "Macro Functions";

    protected ArrayList<Function> funcs = new ArrayList<>();

    public MacroFunctions() {
        super();
    }

    public String getName() {
        return MACRO_FUNCTIONS;
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    public void loadFunctions(Rete engine) {
        UseMacroFunction usemacro = new UseMacroFunction();
        funcs.add(usemacro);
        engine.declareFunction(usemacro);
        // CompileClassMacroFunction compilemacro = new CompileClassMacroFunction();
        // funcs.add(compilemacro);
        // engine.declareFunction(compilemacro);
        GenerateMacroFunction generate = new GenerateMacroFunction();
        funcs.add(generate);
        engine.declareFunction(generate);
        // JarClassMacroFunction jarmacro = new JarClassMacroFunction();
        // funcs.add(jarmacro);
        // engine.declareFunction(jarmacro);
    }
}
