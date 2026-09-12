package org.morendo.rete.functions.bit;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class BitFunctions implements FunctionGroup {

    /** */
    private List<Function> funcs = new ArrayList<>();

    public BitFunctions() {}

    public String getName() {
        return BitFunctions.class.getSimpleName();
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    public void loadFunctions(Rete engine) {
        BitAndFunction bitand = new BitAndFunction();
        engine.declareFunction(bitand);
        funcs.add(bitand);
        BitOrFunction bitor = new BitOrFunction();
        engine.declareFunction(bitor);
        funcs.add(bitor);
        BitNotFunction bitnot = new BitNotFunction();
        engine.declareFunction(bitnot);
        funcs.add(bitnot);
    }
}
