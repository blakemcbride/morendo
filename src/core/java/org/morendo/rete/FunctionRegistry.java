/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.morendo.rete.exception.FunctionException;
import org.morendo.rete.functions.BooleanFunctions;
import org.morendo.rete.functions.DeffunctionGroup;
import org.morendo.rete.functions.IfFunction;
import org.morendo.rete.functions.InterpretedFunction;
import org.morendo.rete.functions.LoadFunctionsFunction;
import org.morendo.rete.functions.RuleEngineFunctions;
import org.morendo.rete.functions.UserDefinedFunctions;
import org.morendo.rete.functions.analysis.AnalysisFunctions;
import org.morendo.rete.functions.bit.BitFunctions;
import org.morendo.rete.functions.cube.CubeFunctions;
import org.morendo.rete.functions.io.IOFunctions;
import org.morendo.rete.functions.java.JavaFunctions;
import org.morendo.rete.functions.list.ListFunctions;
import org.morendo.rete.functions.macro.MacroFunctions;
import org.morendo.rete.functions.math.MathFunctions;
import org.morendo.rete.functions.memory.MemoryFunctions;
import org.morendo.rete.functions.query.QueryFunctions;
import org.morendo.rete.functions.string.StringFunctions;
import org.morendo.rete.functions.temporal.TemporalFunctions;
import org.morendo.rete.functions.text.TextFunctions;
import org.morendo.rete.functions.time.TimeFunctions;
import org.morendo.rete.measures.AggregateGroup;
import org.morendo.rete.measures.Measure;
import org.morendo.rete.measures.MeasureGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The functions and measures an engine knows. The built-in groups are listed in {@link
 * #loadBuiltIns()}; the gui and messaging modules contribute theirs by listing the group class in
 * META-INF/services/org.morendo.rete.FunctionGroup (java.util.ServiceLoader), which any other jar
 * can do as well; groups can also be declared at run time with {@code (load-function-group ...)} /
 * {@link Rete#declareFunctionGroup}. Owned by Rete, which exposes the same operations on its
 * facade.
 */
public class FunctionRegistry {

    private static final Logger log = LogManager.getLogger(FunctionRegistry.class);

    private final Rete engine;

    private final Map<String, Function> functions = new HashMap<>();

    private final List<FunctionGroup> functionGroups = new ArrayList<>();

    private final DeffunctionGroup deffunctions = new DeffunctionGroup();

    private final Map<String, Measure> measures = new HashMap<>();

    private final List<MeasureGroup> measureGroups = new ArrayList<>();

    FunctionRegistry(Rete engine) {
        this.engine = engine;
    }

    /** Registers the built-in function groups, then every group found through ServiceLoader. */
    void loadBuiltIns() {
        FunctionGroup[] builtIns = {
            new AnalysisFunctions(),
            new BitFunctions(),
            new BooleanFunctions(),
            new CubeFunctions(),
            new IOFunctions(),
            new ListFunctions(),
            new TextFunctions(),
            new JavaFunctions(),
            new MathFunctions(),
            new MemoryFunctions(),
            new RuleEngineFunctions(),
            new QueryFunctions(),
            new StringFunctions(),
            new TemporalFunctions(),
            new TimeFunctions(),
            new MacroFunctions()
        };
        for (FunctionGroup group : builtIns) {
            declareGroup(group);
        }
        declare(new IfFunction());
        functionGroups.add(deffunctions);
        UserDefinedFunctions udfs = new UserDefinedFunctions();
        functionGroups.add(udfs);
        LoadFunctionsFunction lff =
                (LoadFunctionsFunction) this.functions.get(LoadFunctionsFunction.LOAD_FUNCTION);
        lff.setUserDefinedFunctions(udfs);
        for (FunctionGroup group : ServiceLoader.load(FunctionGroup.class)) {
            declareGroup(group);
        }
    }

    void loadBuiltInMeasures() {
        AggregateGroup aggregates = new AggregateGroup();
        aggregates.loadMeasures(engine);
        measureGroups.add(aggregates);
    }

    void clear() {
        this.functionGroups.clear();
        this.functions.clear();
    }

    void clearMeasures() {
        this.measureGroups.clear();
        this.measures.clear();
    }

    public Function find(String name) {
        return this.functions.get(name);
    }

    public void declare(Function func) {
        this.functions.put(func.getName(), func);
        if (func instanceof InterpretedFunction) {
            this.deffunctions.addFunction(func);
        }
    }

    public void declare(String alias, Function func) throws FunctionException {
        if (this.functions.containsKey(alias)) {
            throw new FunctionException(
                    alias + " is already in use. Please use a different alias for the function.");
        }
        this.functions.put(alias, func);
    }

    /** Instantiates a Function class by name and declares it; null if it cannot be instantiated. */
    public Function declare(String className) throws ClassNotFoundException {
        try {
            Function func =
                    (Function) Class.forName(className).getDeclaredConstructor().newInstance();
            declare(func);
            return func;
        } catch (ClassNotFoundException e) {
            log.debug(e.toString(), e);
            throw e;
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
            log.debug(e.toString(), e);
            return null;
        }
    }

    public void remove(Function function) {
        this.functions.remove(function.getName());
    }

    public void declareGroup(String className) throws ClassNotFoundException {
        try {
            declareGroup(
                    (FunctionGroup)
                            Class.forName(className).getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException e) {
            log.debug(e.toString(), e);
            throw e;
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
            log.debug(e.toString(), e);
        }
    }

    public void declareGroup(FunctionGroup functionGroup) {
        functionGroup.loadFunctions(engine);
        this.functionGroups.add(functionGroup);
    }

    public void removeGroup(FunctionGroup functionGroup) {
        for (Function func : functionGroup.listFunctions()) {
            this.functions.remove(func.getName());
        }
        this.functionGroups.remove(functionGroup);
    }

    public List<FunctionGroup> groups() {
        return this.functionGroups;
    }

    public Collection<Function> all() {
        return this.functions.values();
    }

    public List<Measure> allMeasures() {
        return new ArrayList<>(this.measures.values());
    }

    public Measure findMeasure(String name) {
        return measures.get(name);
    }

    public void declareMeasure(Measure measure) {
        this.measures.putIfAbsent(measure.getMeasureName(), measure);
    }

    public void declareMeasureGroup(MeasureGroup measureGroup) {
        if (!this.measureGroups.contains(measureGroup)) {
            this.measureGroups.add(measureGroup);
            measureGroup.loadMeasures(engine);
        }
    }
}
