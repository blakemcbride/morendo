/*
 * Copyright 2002-2020 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jamocha.rete;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jamocha.messagerouter.MessageRouter;
import org.jamocha.rete.exception.AssertException;
import org.jamocha.rete.exception.ExecuteException;
import org.jamocha.rete.exception.FunctionException;
import org.jamocha.rete.exception.RetractException;
import org.jamocha.rete.exception.TemplateAssociationException;
import org.jamocha.rete.functions.io.BatchFunction;
import org.jamocha.rete.functions.io.BuildFunction;
import org.jamocha.rete.measures.Measure;
import org.jamocha.rete.measures.MeasureGroup;
import org.jamocha.rete.query.QueryObjTypeNode;
import org.jamocha.rete.util.FactUtils;
import org.jamocha.rete.util.ProfileStats;
import org.jamocha.rule.Defquery;
import org.jamocha.rule.GraphQuery;
import org.jamocha.rule.Query;
import org.jamocha.rule.Rule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Peter Lin
 *     <p>This is the main Rete engine class. For now it's called Rete, but I may change it to
 *     Engine to be more generic.
 */
/**
 * The rule engine: working memory, RETE network, agenda, templates and functions.
 *
 * <p>Threading: an instance is not thread-safe. All calls must come from one thread, or be
 * serialized through the MessageRouter's command thread, which is how the shell and the GUI drive
 * it.
 */
@SuppressWarnings(
        "this-escape") // the router, root node and compilers are created with a reference to the
// engine
public class Rete implements PropertyChangeListener, CompilerListener {

    /** */
    /** What (watch ...) and (unwatch ...) switch on. */
    public enum Watch {
        ACTIVATIONS,
        ALL,
        FACTS,
        RULES
    }

    /** What (profile ...) and (unprofile ...) switch on. */
    public enum Profile {
        ADD_ACTIVATION,
        ASSERT,
        ALL,
        FIRE,
        RETRACT,
        RM_ACTIVATION
    }

    protected boolean halt = true;
    protected int firingcount = 0;
    protected boolean prettyPrint = false;
    protected WorkingMemory workingMem = null;
    private final TemplateRegistry templates = new TemplateRegistry(this);
    private final FunctionRegistry functionRegistry = new FunctionRegistry(this);
    private final EngineOutput output = new EngineOutput(this);

    /**
     * the key is the Class object. The value is the defclass. the defclass is then used to lookup
     * the deftemplate in the current Module.
     */

    /** this is the HashMap for all functions. This means all function names are unique. */

    /** The HashMap for all measures */

    /** an ArrayList for the listeners */
    protected ArrayList<EngineEventListener> listeners = new ArrayList<>();

    private long lastFactId = 1;

    private int lastNodeId = 0;

    // private InterpretedFunction intrFunction = null; Unused
    private Logger log = null;
    private MessageRouter router = new MessageRouter(this);
    protected Deftemplate initFact = new InitialFact();
    private RootNode root = new RootNode(this);
    private RuleCompiler compiler = null;
    private Map<Rule, Object> rulesFired = new HashMap<>();
    private QueryCompiler queryCompiler = null;
    private GraphQueryCompiler graphQueryCompiler = null;
    private Map<String, Query> queries = new HashMap<>();
    private Map<String, GraphQuery> graphQueries = new HashMap<>();
    private final List<Runnable> closeHooks = new ArrayList<>();
    private volatile boolean closed = false;

    /** */
    @SuppressWarnings("unchecked")
    public Rete() {
        super();
        log = LogManager.getLogger(Rete.class);
        this.compiler =
                new DefaultRuleCompiler(
                        this, (Map<Template, ObjectTypeNode>) this.root.getObjectTypeNodes());
        this.queryCompiler =
                new DefaultQueryCompiler(
                        this, (Map<Template, QueryObjTypeNode>) this.root.getObjectTypeNodes());
        this.graphQueryCompiler = new GraphQueryCompiler(this);
        this.workingMem = new DefaultWM(this, root, compiler);
        init();
        startLog();
    }

    @SuppressWarnings("unchecked")
    public Rete(Logger logger) {
        super();
        this.log = logger;
        this.compiler =
                new DefaultRuleCompiler(
                        this, (Map<Template, ObjectTypeNode>) this.root.getObjectTypeNodes());
        this.workingMem = new DefaultWM(this, root, compiler);
        init();
        startLog();
    }

    /** initialization logic should go here */
    protected void init() {
        loadBuiltInFunctions();
        loadBuiltInMeasures();
        declareInitialFact();
        this.declareGraph();
        this.compiler.addListener(this);
    }

    protected void loadBuiltInFunctions() {
        this.functionRegistry.loadBuiltIns();
    }

    protected void loadBuiltInMeasures() {
        this.functionRegistry.loadBuiltInMeasures();
    }

    protected void clearBuiltInFunctions() {
        this.functionRegistry.clear();
    }

    protected void clearBuiltInMeasures() {
        this.functionRegistry.clearMeasures();
    }

    protected void startLog() {
        log.info("Morendo started");
    }

    protected void declareInitialFact() {
        declareTemplate(initFact);
        Deffact ifact = (Deffact) initFact.createFact(null, null, this.nextFactId());
        try {
            this.assertFact(ifact);
        } catch (AssertException e) {
            // an error should not occur
            log.info(e.toString(), e);
        }
    }

    protected void declareGraph() {
        this.declareObject(
                org.jamocha.model.Graph.class, org.jamocha.model.Graph.class.getSimpleName());
        this.declareObject(
                org.jamocha.model.Node.class, org.jamocha.model.Node.class.getSimpleName());
        this.declareObject(
                org.jamocha.model.Edge.class, org.jamocha.model.Edge.class.getSimpleName());
    }

    // ----- methods for clearing rules and facts ----- //

    /** Clear the objects from the working memory */
    public void clearObjects() {
        this.workingMem.clearObjects();
    }

    /**
     * clear the deffacts from the working memory. This does not include facts asserted using
     * assertObject.
     */
    public void clearFacts() {
        this.workingMem.clearFacts();
    }

    /** Iterates over all modules and removes all rules. */
    public synchronized void clearRules() {
        Collection<?> modules = this.workingMem.getModules();
        Iterator<?> iterator = modules.iterator();
        while (iterator.hasNext()) {
            Defmodule mod = (Defmodule) iterator.next();
            mod.removeAllRules(this, this.workingMem);
        }
    }

    /** clear all objects and deffacts */
    public void clearAll() {
        this.workingMem.getDynamicFacts().clear();
        this.workingMem.getStaticFacts().clear();
        this.workingMem.getDeffactMap().clear();
        this.workingMem.clear();

        /**
         * Clear out the modules too. Disturbing the working memory upsets the iteration, so build a
         * list of module names and then remove by name, except MAIN.
         */
        ArrayList<String> modNames = new ArrayList<>();
        Collection<Module> modules = this.workingMem.getModules();
        String modName;
        for (Module mod : modules) {
            modName = mod.getModuleName();
            if (!modName.equals(Constants.MAIN_MODULE)) {
                modNames.add(mod.getModuleName());
                mod.clear();
            }
        }
        for (String mod : modNames) workingMem.removeModule(mod);

        // now we clear all the rules and templates
        this.clearDefclass();
        ProfileStats.reset();
        this.lastFactId = 1;
        this.lastNodeId = 1;
        this.clearBuiltInFunctions();
        this.loadBuiltInFunctions();
        this.clearBuiltInMeasures();
        this.loadBuiltInMeasures();
        FactUtils.reset();
        declareInitialFact();
        declareGraph();
    }

    public void clearDefclass() {
        this.templates.clear();
    }

    /** Method will clear the engine of all rules, facts and objects. */
    /**
     * Registers code to run when the engine is closed, for example to end an application when
     * (exit) is evaluated.
     */
    public void addCloseHook(Runnable hook) {
        this.closeHooks.add(hook);
    }

    /** True once close() has run; the shell stops reading when it sees this. */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Releases the engine: clears working memory and registries, stops the router, runs the close
     * hooks.
     */
    public void close() {
        this.closed = true;
        this.workingMem.clear();
        this.templates.clear();
        this.workingMem.getDeffactMap().clear();
        this.workingMem.getDynamicFacts().clear();
        this.functionRegistry.clear();
        this.workingMem.getInitialFacts().clear();
        this.listeners.clear();
        this.workingMem.getStaticFacts().clear();
        this.router.shutdown();
        for (Runnable hook : this.closeHooks) {
            hook.run();
        }
    }

    protected void addRuleFired(Rule r) {
        this.rulesFired.put(r, null);
    }

    /**
     * this is useful for debugging purposes. clips allows the user to fire 1 rule at a time.
     *
     * @param count
     * @return
     */
    public int fire(int count) throws ExecuteException {
        int counter = 0;
        if (this.workingMem.getCurrentFocus().getActivationCount() > 0) {
            Activation actv = null;
            if (this.workingMem.profileFire()) {
                ProfileStats.startFire();
            }
            while ((actv = this.workingMem.getCurrentFocus().nextActivation(this)) != null
                    && counter < count) {
                try {
                    if (this.workingMem.watchRules()) {
                        this.writeMessage("==> fire: " + actv.toPPString() + "\r\n", "t");
                    }
                    this.pushScope(actv.getRule());
                    actv.executeActivation(this);
                    actv.clear();
                    this.popScope();
                    counter++;
                    this.addRuleFired(actv.getRule());
                } catch (ExecuteException e) {
                    // we need to report the exception
                    log.debug(e.toString(), e);
                    // we break out of the for loop
                    break;
                }
            }
            if (this.workingMem.profileFire()) {
                ProfileStats.endFire();
            }
        }
        return counter;
    }

    /**
     * this is the normal fire. it will fire all the rules that have matched completely.
     *
     * @return
     */
    public int fire() {
        if (this.workingMem.getCurrentFocus().getActivationCount() > 0) {
            // we reset the rules fire count
            this.firingcount = 0;
            Activation actv = null;
            if (this.workingMem.profileFire()) {
                ProfileStats.startFire();
            }
            while ((actv = this.workingMem.getCurrentFocus().nextActivation(this)) != null) {
                try {
                    if (this.workingMem.watchRules()) {
                        this.writeMessage("==> fire: " + actv.toPPString() + "\r\n", "t");
                    }
                    // we push the rule into the scope
                    this.pushScope(actv.getRule());
                    actv.executeActivation(this);
                    actv.clear();
                    this.popScope();
                    this.firingcount++;
                    this.addRuleFired(actv.getRule());
                } catch (ExecuteException e) {
                    log.debug(e.toString(), e);
                }
            }
            if (this.workingMem.profileFire()) {
                ProfileStats.endFire();
            }
            return this.firingcount;
        } else {
            return 0;
        }
    }

    /**
     * method is used to fire an activation immediately
     *
     * @param act
     */
    protected void fireActivation(Activation act) {
        if (act != null) {
            if (this.workingMem.watchRules()) {
                this.writeMessage("==> fire: " + act.toPPString() + "\r\n", "t");
            }
            try {
                this.pushScope(act.getRule());
                act.executeActivation(this);
                act.clear();
                this.popScope();
                this.firingcount++;
                this.addRuleFired(act.getRule());
            } catch (ExecuteException e) {
                log.debug(e.toString(), e);
            }
        }
    }

    /**
     * Method returns a list of the rules that fired
     *
     * @return
     */
    public List<Rule> getRulesFired() {
        ArrayList<Rule> list = new ArrayList<>();
        list.addAll(this.rulesFired.keySet());
        return list;
    }

    public int getRulesFiredCount() {
        return this.firingcount;
    }

    // ----- defmodule related methods ----- //

    /**
     * Method returns the current focus. Only the rules in the current focus will be fired.
     * Activations in other modules will not be fired until the focus is changed to it.
     *
     * @return
     */
    public Module getCurrentFocus() {
        return this.workingMem.getCurrentFocus();
    }

    public boolean addModule(String name) {
        if (this.workingMem.addModule(name) == null) {
            return false;
        } else {
            return true;
        }
    }

    public Module addModule(String name, boolean setfocus) {
        Module mod = this.workingMem.addModule(name);
        if (setfocus) {
            this.workingMem.setCurrentModule(mod);
        }
        return mod;
    }

    public Module removeModule(String name) {
        return this.workingMem.removeModule(name);
    }

    public Module findModule(String name) {
        return this.workingMem.findModule(name);
    }

    /**
     * Add a new Cube definition. Unlike deftemplates, cubes are global and aren't specific to
     * modules. A cube is available to all modules currently defined in the engine.
     *
     * @param cube
     */
    public void addCube(Cube cube) {
        this.workingMem.addCube(cube);
    }

    /**
     * Get the cube by the name
     *
     * @param name
     * @return
     */
    public Cube getCube(String name) {
        return this.workingMem.getCube(name);
    }

    /**
     * Remove the cube from the engine by the name
     *
     * @param name
     * @return
     */
    public Cube removeCube(String name) {
        return this.workingMem.removeCube(name);
    }

    public List<String> getCubes() {
        return this.workingMem.getCubes();
    }

    /**
     * find a function by the name. The name is not the class name. It is the name the function
     * returns in Function.getName().
     *
     * @param name
     * @return
     */
    public Function findFunction(String name) {
        return this.functionRegistry.find(name);
    }

    /**
     * find the template starting with other modules and ending with the main module.
     *
     * @param name
     * @return
     */
    public Template findTemplate(String name) {
        return this.templates.findTemplate(name);
    }

    // -------- method for declaring an object ------------------ //

    /**
     * declare an object using the qualified class name, template and parent. The method will lookup
     * the class. If it fails to find the class, it will throw a ClassNotFoundException.
     */
    public void declareObject(String className, String templateName, String parent)
            throws ClassNotFoundException {
        this.templates.declareObject(className, templateName, parent);
    }

    /**
     * Declare the object using the fully qualified class name for the template name.
     *
     * @param obj
     */
    public void declareObject(Class<?> obj) {
        declareObject(obj, null, null);
    }

    /**
     * Declare a class with a specific template name
     *
     * @param obj
     * @param templateName
     */
    public void declareObject(Class<?> obj, String templateName) {
        declareObject(obj, templateName, null);
    }

    /**
     * @param obj
     * @param templateName
     * @param parent - the parent template
     */
    public void declareObject(Class<?> obj, String templateName, String parent) {
        this.templates.declareObject(obj, templateName, parent);
    }

    /**
     * Method removes class declaration from the engine. First it checks to see if the template is
     * used by a rule. If it is used, the class won't be removed until the rules have been removed
     * also.
     *
     * @param clzz
     */
    public boolean removeObjectType(Class<?> clzz) {
        return this.templates.removeObjectType(clzz);
    }

    /**
     * Declare a cube, so the rule engine can assert cubes and pattern match against it in rules.
     *
     * @param cube
     */
    public void declareCube(Cube cube) {
        this.templates.declareCube(cube);
    }

    /**
     * Convienance method for looking up the Deftemplate for a given Class type. the method is used
     * internally when assertObject(List) is called.
     *
     * @param clazz
     * @return
     */
    public Deftemplate findDeftemplate(Class<?> clazz) {
        return this.templates.findDeftemplate(clazz);
    }

    public Defclass findDeclassByTemplate(String templateName) {
        return this.templates.findDefclassByTemplate(templateName);
    }

    /**
     * In situations where the domain model uses JAXB style objects, we may want to Associate the
     * concrete class with a given deftemplate. if the Clazz object has already been declared, the
     * method will throw an exception.
     *
     * @param clazz
     * @param template
     */
    public void addAssociation(Class<?> clazz, Deftemplate template)
            throws TemplateAssociationException {
        this.templates.addAssociation(clazz, template);
    }

    /**
     * method will try to find the defclass using the Template name. If no defclass is found, method
     * will throw an exception. If it was found, method looks up the Deftemplate using the Class and
     * adds the association.
     *
     * @param clazz
     * @param templateName
     * @throws TemplateAssociationException
     */
    public void addAssociation(Class<?> clazz, String templateName)
            throws TemplateAssociationException {
        this.templates.addAssociation(clazz, templateName);
    }

    /**
     * Lookup the Defclass in the defclass HashMap.
     *
     * @param clazz
     * @return
     */
    public Defclass findDefclass(Class<?> clazz) {
        return this.templates.findDefclass(clazz);
    }

    /**
     * Convienance method for looking up the Defclass by the template name. Some times we use this
     * from functions to quickly lookup the defclass.
     *
     * @param templateName
     * @return
     */
    public Defclass findDefclassByTemplate(String templateName) {
        return this.templates.findDefclassByTemplate(templateName);
    }

    /**
     * Return a Set of the declass instances
     *
     * @return
     */
    public Set<Map.Entry<Object, Defclass>> getDefclasses() {
        return this.templates.getDefclasses();
    }

    /**
     * Implementation will lookup the defclass for a given object by using the Class as the key.
     *
     * @param key
     * @return
     */
    public Defclass findDefclass(Object key) {
        return this.templates.findDefclass(key);
    }

    public Defclass findDefclassByName(String key) {
        return this.templates.findDefclassByName(key);
    }

    /**
     * method is specifically for templates that are declared in the shell and do not have a
     * corresponding java class.
     *
     * @param temp
     */
    public void declareTemplate(Template temp) {
        this.templates.declareTemplate(temp);
    }

    /**
     * To explicitly deploy a custom function, call the method with an instance of the function
     *
     * @param func
     */
    public void declareFunction(Function func) {
        this.functionRegistry.declare(func);
    }

    /**
     * In some cases, we may want to declare a function under an alias. For example, Add can be
     * alias as "+".
     *
     * @param alias
     * @param func
     */
    public void declareFunction(String alias, Function func) throws FunctionException {
        this.functionRegistry.declare(alias, func);
    }

    /**
     * Method will create an instance of the function and declare it. Once a function is declared,
     * it can be used. All custom functions must be declared before they can be used.
     *
     * @param name
     */
    public Function declareFunction(String name) throws ClassNotFoundException {
        return this.functionRegistry.declare(name);
    }

    /**
     * Remove a function using the Function class
     *
     * @param function
     */
    public void removeFunction(Function function) {
        this.functionRegistry.remove(function);
    }

    /**
     * Method will create in instance of the FunctionGroup class and load the functions.
     *
     * @param name
     */
    public void declareFunctionGroup(String name) throws ClassNotFoundException {
        this.functionRegistry.declareGroup(name);
    }

    /**
     * Method will register the function of the FunctionGroup .
     *
     * @param functionGroup FunctionGroup with the functions to register.
     */
    public void declareFunctionGroup(FunctionGroup functionGroup) {
        this.functionRegistry.declareGroup(functionGroup);
    }

    public void removeFunctionGroup(FunctionGroup functionGroup) {
        this.functionRegistry.removeGroup(functionGroup);
    }

    /**
     * Returns a list of the function groups. If a function is not in a group, get the complete list
     * of functions using getAllFunctions instead.
     *
     * @return
     */
    public List<FunctionGroup> getFunctionGroups() {
        return this.functionRegistry.groups();
    }

    /**
     * Returns a collection of the function instances
     *
     * @return
     */
    public Collection<Function> getAllFunctions() {
        return this.functionRegistry.all();
    }

    public void declareDefquery(Query query) {
        if (!this.queries.containsKey(query.getName())) {
            this.queries.put(query.getName(), query);
        }
    }

    public void declareGraphQuery(GraphQuery query) {
        if (!this.graphQueries.containsKey(query.getName())) {
            this.graphQueries.put(query.getName(), query);
        }
    }

    public Query getDefquery(String name) {
        return ((Defquery) this.queries.get(name)).clone(this);
    }

    public Query removeDefquery(String name) {
        return this.queries.remove(name);
    }

    public GraphQuery getGraphQuery(String name) {
        return this.graphQueries.get(name);
    }

    public GraphQuery removeGraphQuery(String name) {
        return this.graphQueries.remove(name);
    }

    public List<Measure> getAllMeasures() {
        return this.functionRegistry.allMeasures();
    }

    public Measure findMeasure(String name) {
        return this.functionRegistry.findMeasure(name);
    }

    public void declareMeasure(Measure measure) {
        this.functionRegistry.declareMeasure(measure);
    }

    public void declareMeasureGroup(MeasureGroup measureGroup) {
        this.functionRegistry.declareMeasureGroup(measureGroup);
    }

    // ------------- Methods for loading the ruleset ------------------ //

    /**
     * pass a filename to load the rules. The implementation uses BatchFunction to load the file.
     *
     * @param filename
     */
    public void loadRuleset(String filename) {
        BatchFunction bf = (BatchFunction) findFunction(BatchFunction.BATCH);
        Parameter[] params = new Parameter[] {new ValueParam(ValueType.STRING, filename)};
        bf.executeFunction(this, params);
    }

    /**
     * load the rules from an inputstream. The implementation uses the Batch function to load the
     * input.
     *
     * @param ins
     */
    public void loadRuleset(InputStream ins) {
        if (ins != null) {
            BatchFunction bf = (BatchFunction) findFunction(BatchFunction.BATCH);
            bf.parse(this, ins, null);
        }
    }

    public RootNode getRootNode() {
        return this.root;
    }

    public void declareDefglobal(String name, Object value) {
        this.workingMem.getDefglobals().declareDefglobal(name, value);
    }

    public Object getDefglobalValue(String name) {
        return this.workingMem.getDefglobals().getValue(name);
    }

    public DefglobalMap getDefglobalMap() {
        return this.workingMem.getDefglobals();
    }

    public void removeDefglobal(String name) {
        this.workingMem.getDefglobals().removeDefglobal(name);
    }

    /**
     * build method will take text and pass it to the parser
     *
     * @param text
     */
    public void build(String text) {
        Function f = this.findFunction(BuildFunction.BUILD);
        ValueParam p = new ValueParam(ValueType.STRING, text);
        Parameter[] params = new Parameter[] {p};
        f.executeFunction(this, params);
    }

    // -------------- Get / Set methods --------------------- //

    /**
     * The current implementation will check to see if the variable is a defglobal. If it is, it
     * will return the value. If not, it will see if there is an active rule and try to get the
     * local bound value.
     *
     * @param name
     * @return
     */
    public Object getBinding(String name) {
        return this.workingMem.getBinding(name);
    }

    /**
     * This is the main method for setting the bindings. The current implementation will check to
     * see if the name of the variable begins and ends with "*". If it does, it will declare it as a
     * defglobal. Otherwise, it will try to add it to the rule being fired. Note: might need to have
     * add one for shell variables later.
     *
     * @param key
     * @param value
     */
    public void setBindingValue(String key, Object value) {
        this.workingMem.setBindingValue(key, value);
    }

    /**
     * when a rule is active, it should push itself into the scopes. when the rule is done, it has
     * to pop itself out of scope. The same applies to interpretedFunctions.
     *
     * @param s
     */
    public void pushScope(Scope s) {
        this.workingMem.pushScope(s);
    }

    /** pop a scope out of the stack */
    public void popScope() {
        this.workingMem.popScope();
    }

    /* TODO - check if might be used
    public void setInterpretedFunction(InterpretedFunction f) {
        this.intrFunction = f;
    }
    */

    /**
     * set the focus to a different module
     *
     * @param moduleName
     */
    public void setFocus(String moduleName) {
        Module mod = this.workingMem.findModule(moduleName);
        if (mod != null) {
            this.workingMem.setCurrentModule(mod);
        }
    }

    /**
     * Rete class contains a list of items that can be watched. Call the method with one of the four
     * types:<br>
     * activations<br>
     * all<br>
     * facts<br>
     * rules<br>
     *
     * @param type
     */
    public void setWatch(Watch what) {
        watch(what, true);
    }

    /**
     * Call the method with the type to unwatch activations<br>
     * facts<br>
     * rules<br>
     *
     * @param type
     */
    public void setUnWatch(Watch what) {
        watch(what, false);
    }

    private void watch(Watch what, boolean on) {
        switch (what) {
            case ACTIVATIONS -> this.workingMem.getAgenda().setWatch(on);
            case FACTS -> this.workingMem.setWatchFact(on);
            case RULES -> this.workingMem.setWatchRules(on);
            case ALL -> {
                this.workingMem.getAgenda().setWatch(on);
                this.workingMem.setWatchFact(on);
                this.workingMem.setWatchRules(on);
            }
        }
    }

    public void setWatchQuery(String name) {
        Query q = this.queries.get(name);
        if (q != null) {
            q.setWatch(true);
        } else {
            GraphQuery gq = this.getGraphQuery(name);
            if (gq != null) {
                gq.setWatch(true);
            }
        }
    }

    public void setUnWatchQuery(String name) {
        Query q = this.queries.get(name);
        if (q != null) {
            q.setWatch(false);
        }
    }

    public void setQueryTime(String name, long time) {
        Query q = this.queries.get(name);
        if (q != null) {
            ((Defquery) q).setElapsedTime(time);
        }
    }

    public long getQueryTime(String name) {
        Query q = this.queries.get(name);
        if (q != null) {
            return ((Defquery) q).getElapsedTime();
        }
        return 0;
    }

    /**
     * To turn on profiling, call the method with the appropriate parameter. The parameters are
     * defined in Rete class as static int values.
     *
     * @param type
     */
    public void setProfile(Profile what) {
        profile(what, true);
    }

    /**
     * To turn off profiling, call the method with the appropriate parameter. The parameters are
     * defined in Rete class as static int values.
     *
     * @param type
     */
    public void setProfileOff(Profile what) {
        profile(what, false);
    }

    private void profile(Profile what, boolean on) {
        switch (what) {
            case ADD_ACTIVATION -> this.workingMem.getAgenda().setProfileAdd(on);
            case ASSERT -> this.workingMem.setProfileAssert(on);
            case FIRE -> this.workingMem.setProfileFire(on);
            case RETRACT -> this.workingMem.setProfileRetract(on);
            case RM_ACTIVATION -> this.workingMem.getAgenda().setProfileRemove(on);
            case ALL -> {
                this.workingMem.getAgenda().setProfileAdd(on);
                this.workingMem.setProfileAssert(on);
                this.workingMem.setProfileFire(on);
                this.workingMem.setProfileRetract(on);
                this.workingMem.getAgenda().setProfileRemove(on);
            }
        }
    }

    // --------------- methods for getting facts and counts ----------------- //

    /**
     * return a list of all the facts including deffacts and shadow of objects
     *
     * @return
     */
    public List<Fact> getAllFacts() {
        return this.workingMem.getAllFacts();
    }

    /**
     * Return a list of the objects asserted in the working memory
     *
     * @return
     */
    public List<Object> getObjects() {
        return this.workingMem.getObjects();
    }

    /**
     * Return a list of all facts which are not shadows of Objects.
     *
     * @return
     */
    public List<?> getDeffacts() {
        return this.workingMem.getDeffacts();
    }

    /**
     * return just the number of deffacts
     *
     * @return
     */
    public int getDeffactCount() {
        return this.workingMem.getDeffactMap().size();
    }

    /**
     * get the shadow for the object
     *
     * @param key
     * @return
     */
    public Fact getShadowFact(Object key) {
        Fact f = (Fact) this.workingMem.getDynamicFacts().get(key);
        if (f == null) {
            f = (Fact) this.workingMem.getStaticFacts().get(key);
        }
        return f;
    }

    /**
     * changed the implementation so it searches for the fact by id. Starting with the HashMap for
     * deffact, dynamic facts and finally static facts.
     *
     * @param id
     * @return
     */
    public Fact getFactById(long id) {
        return this.workingMem.getFactById(id);
    }

    // ----- method for adding output streams for spools ----- //
    /**
     * this method is for adding printwriters for spools. the purpose of the spool function is to
     * dump everything out to a file.
     */
    public void addPrintWriter(String name, Writer writer) {
        this.output.addPrintWriter(name, writer);
    }

    /**
     * It is up to spool function to make sure it removes the printer writer and closes it properly.
     *
     * @param name
     * @return
     */
    public PrintWriter removePrintWriter(String name) {
        return this.output.removePrintWriter(name);
    }

    // ----- method for writing messages out ----- //
    /**
     * The method is called by classes to write watch, profiling and other messages to the output
     * stream. There maybe 1 or more outputstreams.
     *
     * @param msg
     */
    public void writeMessage(String msg) {
        writeMessage(msg, "t");
    }

    /**
     * writeMessage will create a MessageEvent and pass it along to any channels. It will also write
     * out all messages to all registered PrintWriters. For example, if there's a spool setup, it
     * will write the messages to the printwriter.
     *
     * @param msg
     * @param output
     */
    public void writeMessage(String msg, String output) {
        this.output.write(msg, output);
    }

    /**
     * The method will print out the node. It is up to the method to check if pretty printer is true
     * and call the appropriate node method to get the string. TODO - need to implement this
     *
     * @param node
     */
    public void writeMessage(BaseNode node) {}

    // ------------------ methods for assert, retract and modify facts ----------------- //

    /**
     * the method calls WorkingMemory.assertObject
     *
     * @param data
     * @param template
     * @param statc
     * @param shadow
     * @throws AssertException
     */
    public void assertObject(Object data, String template, boolean statc, boolean shadow)
            throws AssertException {
        this.workingMem.assertObject(data, template, statc, shadow);
    }

    /**
     * the method is used to assert temporal objects. Call the method with the an optional effective
     * time and required expiration time.
     *
     * @param data
     * @param template
     * @param effective
     * @param expiration
     * @param statc
     * @throws AssertException
     */
    public void assertTemporalObject(
            Object data, String template, Instant effective, Instant expiration, boolean statc)
            throws AssertException {
        this.workingMem.assertTemporalObject(data, template, effective, expiration, statc);
    }

    /**
     * By default assertObjects will assert with shadow and dynamic. It also assumes the classes
     * aren't using an user defined template name.
     *
     * @param objs
     * @throws AssertException
     */
    public void assertObjects(List<?> objs) throws AssertException {
        this.workingMem.assertObjects(objs);
    }

    /**
     * @param data
     */
    public void retractObject(Object data) throws RetractException {
        this.workingMem.retractObject(data);
    }

    /**
     * Modify will call retract with the old fact, followed by updating the fact instance and
     * asserting the fact.
     *
     * @param data
     */
    public void modifyObject(Object data) throws AssertException, RetractException {
        this.workingMem.modifyObject(data);
    }

    /**
     * This method is explicitly used to assert facts.
     *
     * @param fact
     * @param statc - if the fact should be static, assert with true
     */
    public void assertFact(Fact fact) throws AssertException {
        this.workingMem.assertFact(fact);
    }

    /**
     * Method is for asserting a temporal fact
     *
     * @param fact
     * @param expirationTime
     * @throws AssertException
     */
    public void assertFact(TemporalFact fact, Instant effectiveTime, Instant expirationTime)
            throws AssertException {
        this.workingMem.assertFact(fact, effectiveTime, expirationTime);
    }

    /**
     * retract by fact id is slower than retracting by the deffact instance. the method will find
     * the fact and then call retractFact(Deffact)
     *
     * @param id
     */
    public void retractById(long id) throws RetractException {
        Iterator<?> itr = this.workingMem.getDeffactMap().values().iterator();
        Fact ft = null;
        while (itr.hasNext()) {
            Fact f = (Fact) itr.next();
            if (f.getFactId() == id) {
                ft = f;
                break;
            }
        }
        if (ft != null) {
            retractFact(ft);
        }
    }

    /**
     * Retract a fact directly
     *
     * @param fact
     * @throws RetractException
     */
    public void retractFact(Fact fact) throws RetractException {
        this.workingMem.retractFact(fact);
    }

    /**
     * Modify retracts the old fact and asserts the new fact. Unlike assertFact, modifyFact will not
     * check to see if the fact already exists. This is because the old fact would already be
     * unique.
     *
     * @param old
     * @param newfact
     */
    public void modifyFact(Fact old, Fact newfact) throws RetractException, AssertException {
        retractFact(old);
        assertFact(newfact);
    }

    // -------------- method for reseting the rule engine ----------------- //

    /** Method will call resetObjects first, followed by resetFacts. */
    public void resetAll() {
        ProfileStats.reset();
        resetObjects();
        resetFacts();
    }

    /** Method will retract the objects and re-assert them. It does not reset the deffacts. */
    public void resetObjects() {
        try {
            this.workingMem.getAgenda().startReset();

            Iterator<?> itr = this.workingMem.getStaticFacts().values().iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.retractFact(ft);
            }
            itr = this.workingMem.getDynamicFacts().values().iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.retractFact(ft);
            }
            // now assert
            this.workingMem.getAgenda().endReset();

            itr = this.workingMem.getStaticFacts().values().iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.assertFact(ft);
            }
            itr = this.workingMem.getDynamicFacts().values().iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.assertFact(ft);
            }
        } catch (RetractException e) {
            log.debug(e.toString(), e);
        } catch (AssertException e) {
            log.debug(e.toString(), e);
        }
    }

    /**
     * Method will retract all the deffacts and then re-assert them. Reset does not reset the
     * objects. To reset both the facts and objects, call resetAll. resetFacts handles deffacts
     * which are not derived from objects.
     */
    public void resetFacts() {
        try {
            this.workingMem.getAgenda().startReset();

            List<?> facts = new ArrayList<>(this.workingMem.getDeffactMap().values());
            Iterator<?> itr = facts.iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.retractFact(ft);
            }

            // now assert
            this.workingMem.getAgenda().endReset();

            itr = facts.iterator();
            while (itr.hasNext()) {
                Fact ft = (Fact) itr.next();
                this.workingMem.assertFact(ft);
            }
        } catch (RetractException e) {
            log.debug(e.toString(), e);
        } catch (AssertException e) {
            log.debug(e.toString(), e);
        }
    }

    /**
     * This is temporary, it should be replaced with something like the current
     * factHandleFactory().newFactHandle()
     *
     * @return
     */
    public long nextFactId() {
        return this.lastFactId++;
    }

    public Agenda getAgenda() {
        return this.workingMem.getAgenda();
    }

    /**
     * return the next rete node id for a new node
     *
     * @return
     */
    public int nextNodeId() {
        return ++this.lastNodeId;
    }

    /**
     * peak at the next node id. Do not use this method to get an id for the next node. only
     * nextNodeId() should be used to create new rete nodes.
     *
     * @return
     */
    public int peakNextNodeId() {
        return this.lastNodeId + 1;
    }

    public RuleCompiler getRuleCompiler() {
        return this.workingMem.getRuleCompiler();
    }

    public QueryCompiler getQueryCompiler() {
        return this.queryCompiler;
    }

    public GraphQueryCompiler getGraphQueryCompiler() {
        return this.graphQueryCompiler;
    }

    public WorkingMemory getWorkingMemory() {
        return this.workingMem;
    }

    public Strategy getStrategy() {
        return this.workingMem.getStrategy();
    }

    public ActivationList getActivationList() {
        return this.workingMem.getCurrentFocus().getAllActivations();
    }

    public int getObjectCount() {
        return this.workingMem.getDynamicFacts().size() + this.workingMem.getStaticFacts().size();
    }

    public void setValidateRules(boolean val) {
        this.workingMem.getRuleCompiler().setValidateRule(val);
    }

    public boolean getValidateRules() {
        return this.workingMem.getRuleCompiler().getValidateRule();
    }

    /// Map methods
    public <K, V> Map<K, V> newMap() {
        return new HashMap<>();
    }

    public <K, V> Map<K, V> newLocalMap() {
        return new HashMap<>();
    }

    public <K, V> Map<K, V> newAlphaMemoryMap(String name) {
        return new HashMap<>();
    }

    public <K, V> Map<K, V> newLinkedHashmap(String name) {
        return new LinkedHashMap<>();
    }

    public <K, V> Map<K, V> newBetaMemoryMap(String name) {
        return new HashMap<>();
    }

    public <K, V> Map<K, V> newTerminalMap() {
        return new HashMap<>();
    }

    public <K, V> Map<K, V> newClusterableMap(String name) {
        return new HashMap<>();
    }

    /**
     * Method calls modifyObject to notify the engine the object has changed. No optimization at
     * this point in time. Later on we can check to make sure the object actually changed, but that
     * shouldn't be necessary if the class checks the field before calling propertyChange.
     *
     * @param event
     */
    public void propertyChange(PropertyChangeEvent event) {
        Object source = event.getSource();
        try {
            this.modifyObject(source);
        } catch (RetractException e) {
            log.debug(e.toString(), e);
        } catch (AssertException e) {
            log.debug(e.toString(), e);
        }
    }

    /**
     * Add a listener if it isn't already a listener
     *
     * @param listen
     */
    public void addEngineEventListener(EngineEventListener listen) {
        if (!this.listeners.contains(listen)) {
            this.listeners.add(listen);
        }
    }

    /**
     * remove a listener
     *
     * @param listen
     */
    public void removeEngineEventListener(EngineEventListener listen) {
        this.listeners.remove(listen);
    }

    /**
     * For now, this is not implemented
     *
     * @param event
     */
    public void ruleAdded(CompileEvent event) {
        this.log.info("added: " + event.getMessage());
    }

    /**
     * For now, this is not implemented
     *
     * @param event
     */
    public void ruleRemoved(CompileEvent event) {
        this.log.info("removed: " + event.getMessage());
    }

    /**
     * For now, this is not implemented
     *
     * @param event
     */
    public void compileError(CompileEvent event) {
        this.log.warn(event.getMessage());
    }

    public MessageRouter getMessageRouter() {
        return router;
    }

    public Deftemplate getInitFact() {
        return initFact;
    }
}
