package org.morendo.rete.functions.temporal;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class TemporalFunctions implements FunctionGroup {

    /** */
    private ArrayList<Function> funcs = new ArrayList<>();

    public TemporalFunctions() {
        super();
    }

    public String getName() {
        return TemporalFunctions.class.getSimpleName();
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    public void loadFunctions(Rete engine) {
        CalculateTemporalDistanceFunction ctd = new CalculateTemporalDistanceFunction();
        funcs.add(ctd);
        engine.declareFunction(ctd);
        SetTemporalDistanceFunction setdist = new SetTemporalDistanceFunction();
        funcs.add(setdist);
        engine.declareFunction(setdist);
    }
}
