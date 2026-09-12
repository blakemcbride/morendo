package org.jamocha.gui.functions;

import org.jamocha.rete.Function;
import org.jamocha.rete.FunctionGroup;
import org.jamocha.rete.Rete;

import java.util.ArrayList;
import java.util.List;

/**
 * The functions the GUI module adds to an engine, registered through
 * META-INF/services/org.jamocha.rete.FunctionGroup when the module is on the classpath.
 */
public class GuiFunctions implements FunctionGroup {

    private final List<Function> funcs = new ArrayList<>();

    public String getName() {
        return GuiFunctions.class.getSimpleName();
    }

    public void loadFunctions(Rete engine) {
        ViewFunction view = new ViewFunction();
        engine.declareFunction(view);
        funcs.add(view);
    }

    public List<Function> listFunctions() {
        return funcs;
    }
}
