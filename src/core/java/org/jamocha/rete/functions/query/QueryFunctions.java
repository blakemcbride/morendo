package org.jamocha.rete.functions.query;

import org.jamocha.rete.Function;
import org.jamocha.rete.FunctionGroup;
import org.jamocha.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class QueryFunctions implements FunctionGroup {

    /** */
    private ArrayList<Function> funcs = new ArrayList<>();

    public QueryFunctions() {
        super();
    }

    public String getName() {
        return QueryFunctions.class.getSimpleName();
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    public void loadFunctions(Rete engine) {
        DefqueryFunction dquery = new DefqueryFunction();
        engine.declareFunction(dquery);
        funcs.add(dquery);
        QueryTimeFunction queryTime = new QueryTimeFunction();
        engine.declareFunction(queryTime);
        funcs.add(queryTime);
        RunQueryFunction runquery = new RunQueryFunction();
        engine.declareFunction(runquery);
        funcs.add(runquery);
        RunGraphQueryFunction rungr = new RunGraphQueryFunction();
        engine.declareFunction(rungr);
        funcs.add(rungr);
        UnWatchQueryFunction unwatch = new UnWatchQueryFunction();
        engine.declareFunction(unwatch);
        funcs.add(unwatch);
        WatchQueryFunction watchq = new WatchQueryFunction();
        engine.declareFunction(watchq);
        funcs.add(watchq);
        DefGraphQueryFunction dgquery = new DefGraphQueryFunction();
        engine.declareFunction(dgquery);
        funcs.add(dgquery);
    }
}
