/*
 * Copyright 2002-2009 Peter Lin
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
package org.morendo.rete;

import org.morendo.rete.exception.AssertException;
import org.morendo.rete.exception.RetractException;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Peter Lin
 *     <p>Interface defining working memory, which contains all HashMaps and data structures related
 *     to the node memories, profiling, binding and scopes.
 */
public interface WorkingMemory {

    /// ----- assert and retract methods ----- ///
    void assertFact(Fact fact) throws AssertException;

    public void assertFact(TemporalFact fact, Instant effectiveTime, Instant expirationTime)
            throws AssertException;

    public void assertObject(Object data, String template, boolean statc, boolean shadow)
            throws AssertException;

    public void assertTemporalObject(
            Object data, String template, Instant effective, Instant expiration, boolean statc)
            throws AssertException;

    void assertObjects(List<?> objs) throws AssertException;

    void retractFact(Fact fact) throws RetractException;

    public void retractObject(Object data) throws RetractException;

    public void modifyObject(Object data) throws AssertException, RetractException;

    public Agenda getAgenda();

    public Map<Object, Object> getDeffactMap();

    public DefglobalMap getDefglobals();

    public List<Fact> getAllFacts();

    public List<Fact> getDeffacts();

    public Fact getFactById(long id);

    public List<Object> getObjects();

    List<Fact> getInitialFacts();

    public boolean profileAssert();

    public void setProfileAssert(boolean profileAssert);

    public boolean profileFire();

    public void setProfileFire(boolean profileFire);

    public boolean profileRetract();

    public void setProfileRetract(boolean profileRetract);

    public boolean watchFact();

    public void setWatchFact(boolean watchFact);

    public boolean watchRules();

    public void setWatchRules(boolean watchRules);

    Object getBinding(String name);

    void setBindingValue(String key, Object value);

    void pushScope(Scope s);

    void popScope();

    /// ----- methods for getting the Fact map  ----- ///
    Map<Object, Object> getDynamicFacts();

    Map<Object, Object> getStaticFacts();

    /// ----- methods related to cube ----- ///
    void addCube(Cube cube);

    Cube getCube(String name);

    Cube removeCube(String name);

    List<String> getCubes();

    /// ----- methods related to module ----- ///
    public Module addModule(String name);

    public Module findModule(String name);

    public Module getCurrentFocus();

    public Module getMain();

    public Module removeModule(String name);

    public void setCurrentModule(Module mod);

    public Collection<Module> getModules();

    /** Makes the module the focus, remembering the current one for {@link #popFocus()}. */
    public void pushFocus(Module mod);

    /** Restores the focus pushed last; false when nothing was pushed. */
    public boolean popFocus();

    /** Drops the alpha, left and right memories of the given query nodes. */
    public void clearQueryMemories(java.util.Collection<?> nodes);

    /// ----- method for Strategy ----- ///
    public Strategy getStrategy();

    public void setStrategy(Strategy strategy);

    /// -----  methods for getting the memory for a given node ----- ///
    /**
     * The key for looking up the memory should be the node. Each node should pass itself as the key
     * for the lookup.
     *
     * @param key
     * @return
     */
    <T> T getAlphaMemory(Object key);

    /**
     * In the case of AlphaMemory, during the compilation process, we may want to remove an alpha
     * memory if one already exists. This depends on how rule compilation works.
     *
     * @param key
     */
    void removeAlphaMemory(Object key);

    /**
     * The key for the lookup should be the node. Each BetaNode has a left and right memory, so it's
     * necessary to have a lookup method for each memory.
     *
     * @param key
     * @return
     */
    <T> T getBetaLeftMemory(Object key);

    /**
     * The key for the lookup should be the node. Each BetaNode has a left and right memory, so it's
     * necessary to have a lookup method for each memory.
     *
     * @param key
     * @return
     */
    <T> T getBetaRightMemory(Object key);

    /**
     * The for the lookup is the terminalNode. Depending on the terminal node used, it may not have
     * a memory.
     *
     * @param key
     * @return
     */
    Map<Index, Activation> getTerminalMemory(Object key);

    /**
     * Returns the beta memory for query join nodes
     *
     * @param key
     * @return
     */
    <T> T getQueryBetaMemory(Object key);

    /**
     * Returns the right memory for query join nodes
     *
     * @param key
     * @return
     */
    <T> T getQueryRightMemory(Object key);

    /**
     * Return the RuleCompiler for this working memory
     *
     * @return
     */
    RuleCompiler getRuleCompiler();

    /** Clears everything in the working memory */
    void clear();

    public void clearObjects();

    public void clearFacts();
}
