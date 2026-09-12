package org.morendo.rete.functions.control;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

/**
 * Loops, sequencing and early exits: progn, while, loop-for-count, foreach, progn$, break, return,
 * halt.
 */
public class ControlFunctions implements FunctionGroup {

    private final ArrayList<Function> funcs = new ArrayList<>();

    public String getName() {
        return ControlFunctions.class.getSimpleName();
    }

    public void loadFunctions(Rete engine) {
        Function[] all = {
            new PrognFunction(),
            new WhileFunction(),
            new LoopForCountFunction(),
            new ForeachFunction(),
            new PrognDollarFunction(),
            new BreakFunction(),
            new ReturnFunction(),
            new HaltFunction()
        };
        for (Function f : all) {
            engine.declareFunction(f);
            funcs.add(f);
        }
    }

    public List<Function> listFunctions() {
        return funcs;
    }
}
