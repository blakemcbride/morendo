package org.morendo.rete.functions.text;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

/**
 * TextFunctions contains functions for text parsing like stop words and doing a simple word count.
 * These are generic base functions for processing text before you do analytics.
 *
 * @author peter
 */
public class TextFunctions implements FunctionGroup {

    /** */
    private ArrayList<Function> funcs = new ArrayList<>();

    public TextFunctions() {
        super();
    }

    public String getName() {
        return TextFunctions.class.getSimpleName();
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    public void loadFunctions(Rete engine) {
        StopwordFunction swf = new StopwordFunction();
        funcs.add(swf);
        engine.declareFunction(swf);
        TokenizeFunction tnf = new TokenizeFunction();
        funcs.add(tnf);
        engine.declareFunction(tnf);
        TokenMatchFunction tm = new TokenMatchFunction();
        funcs.add(tm);
        engine.declareFunction(tm);
    }
}
