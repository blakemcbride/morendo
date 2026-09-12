package org.morendo.rete.functions;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class UserDefinedFunctions implements FunctionGroup {

    /** */
    private ArrayList<Function> funcs = new ArrayList<>();

    public static final String USER_DEFINED_FUNCTIONS = "User Defined Functions";

    public UserDefinedFunctions() {}

    public String getName() {
        return USER_DEFINED_FUNCTIONS;
    }

    public List<Function> listFunctions() {
        return this.funcs;
    }

    /**
     * method is not implemented, since user defined functions aren't loaded by the engine. They are
     * loaded by the user.
     */
    public void loadFunctions(Rete engine) {}

    public void addFunction(Function f) {
        this.funcs.add(f);
    }
}
