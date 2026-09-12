/*
 * Copyright 2002-2010 Peter Lin
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
package org.morendo.rete.query;

import org.morendo.rete.BaseNode;
import org.morendo.rete.Constants;
import org.morendo.rete.Fact;
import org.morendo.rete.Rete;
import org.morendo.rete.RootNode;
import org.morendo.rete.Template;
import org.morendo.rete.WorkingMemory;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.exception.RetractException;
import org.morendo.rule.Defquery;

import java.util.Iterator;
import java.util.Map;

/**
 * QueryRootNode does not extend BaseNode like all other RETE nodes. This is done for a couple of
 * reasons.<br>
 *
 * <ul>
 *   <li>RootNode doesn't need to have a memory
 *   <li>RootNode only has QueryObjectTypeNode for successors
 *   <li>RootNode doesn't need the toPPString and other string methods
 * </ul>
 *
 * In the future, the design may change. For now, I've decided to keep it as simple as necessary.
 *
 * @author Peter Lin
 */
public class QueryRootNode {

    /** */
    protected Map<Template, QueryObjTypeNode> queryObjTypeNodeMap = null;

    protected RootNode root = null;
    protected QueryObjTypeNode initialFactObjTypeNode = null;

    /** */
    public QueryRootNode(Rete engine, RootNode root) {
        super();
        queryObjTypeNodeMap = engine.newMap();
        this.root = root;
    }

    public RootNode getRootNode() {
        return this.root;
    }

    /**
     * Add a new ObjectTypeNode. The implementation will check to see if the node already exists. It
     * will only add the node if it doesn't already exist in the network.
     *
     * @param node
     */
    public void addQueryObjTypeNode(QueryObjTypeNode node) {
        if (node.getDeftemplate().getName().equals(Constants.INITIAL_FACT)) {
            this.initialFactObjTypeNode = node;
        } else {
            if (!this.queryObjTypeNodeMap.containsKey(node.getDeftemplate())) {
                this.queryObjTypeNodeMap.put(node.getDeftemplate(), node);
            }
        }
    }

    /**
     * The current implementation just removes the ObjectTypeNode and doesn't prevent the removal.
     * The method should be called with care, since removing the ObjectTypeNode can have serious
     * negative effects. This would generally occur when an undeftemplate occurs.
     */
    public void removeQueryObjTypeNode(QueryObjTypeNode node) {
        this.queryObjTypeNodeMap.remove(node.getDeftemplate());
    }

    /**
     * Return the HashMap with all the ObjectTypeNodes
     *
     * @return
     */
    /** Every node of the query network, found by walking the successors of the type nodes. */
    public java.util.List<BaseNode> getAllNodes() {
        java.util.List<BaseNode> nodes = new java.util.ArrayList<>();
        java.util.Set<BaseNode> seen =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        java.util.ArrayDeque<BaseNode> todo =
                new java.util.ArrayDeque<>(this.queryObjTypeNodeMap.values());
        while (!todo.isEmpty()) {
            BaseNode node = todo.pop();
            if (seen.add(node)) {
                nodes.add(node);
                for (Object next : node.getSuccessorNodes()) {
                    if (next instanceof BaseNode child) {
                        todo.push(child);
                    }
                }
            }
        }
        return nodes;
    }

    /** Forgets the matches of the previous run, so the network can be executed again. */
    public void clearMemories(WorkingMemory mem) {
        mem.clearQueryMemories(getAllNodes());
    }

    public Map<Template, QueryObjTypeNode> getQueryObjTypeNodes() {
        return this.queryObjTypeNodeMap;
    }

    /**
     * Method returns the QueryObjTypeNode so the Query compiler can add child nodes to it.
     *
     * @param template
     * @return
     */
    public QueryObjTypeNode findQueryObjTypeNode(Template template) {
        return this.queryObjTypeNodeMap.get(template);
    }

    /**
     * Important note, QueryRootNode doesn't actually propogate the fact down the query network. The
     * QueryObjTypeNodes get the list of facts from the corresponding ObjectTypeNode.
     *
     * @param fact
     * @param engine
     * @param mem
     * @throws AssertException
     */
    public synchronized void assertObject(Fact fact, Rete engine, WorkingMemory mem)
            throws AssertException {
        if (fact != null) {
            QueryObjTypeNode qotn = this.findQueryObjTypeNode(fact.getDeftemplate());
            if (qotn != null) {
                qotn.assertFact(fact, engine, mem);
            }
        } else {
            Iterator<Template> iterator = this.queryObjTypeNodeMap.keySet().iterator();
            while (iterator.hasNext()) {
                Template template = iterator.next();
                QueryObjTypeNode qotn = this.queryObjTypeNodeMap.get(template);
                if (qotn != null) {
                    qotn.assertFact(null, engine, mem);
                }
                if (template.getParent() != null) {
                    assertObjectParent(template.getParent(), engine, mem);
                }
            }
        }
    }

    /**
     * Method will get the deftemplate's parent and do a lookup
     *
     * @param fact
     * @param templates
     * @throws AssertException
     */
    public synchronized void assertObjectParent(Template template, Rete engine, WorkingMemory mem)
            throws AssertException {
        QueryObjTypeNode otn = this.queryObjTypeNodeMap.get(template);
        if (otn != null) {
            otn.assertFact(null, engine, mem);
        }
        if (template.getParent() != null) {
            assertObjectParent(template.getParent(), engine, mem);
        }
    }

    /**
     * Retract an object from the Working memory
     *
     * @param objInstance
     */
    public synchronized void retractObject(Fact fact, Rete engine, WorkingMemory mem)
            throws RetractException {}

    /**
     * Method will get the deftemplate's parent and do a lookup
     *
     * @param fact
     * @param templates
     * @throws AssertException
     */
    public synchronized void retractObjectParent(
            Fact fact, Template template, Rete engine, WorkingMemory mem) throws RetractException {}

    public synchronized void clear() {
        for (QueryObjTypeNode otn : this.queryObjTypeNodeMap.values()) {
            otn.removeAllSuccessors();
        }
        this.queryObjTypeNodeMap.clear();
    }

    /**
     * Clone method takes the engine and the new clone of the existing Defquery instance. This is
     * because we need to add the nodes to the clone instance during the clone process.
     *
     * @param engine
     * @param query
     * @return
     */
    public QueryRootNode clone(Rete engine, Defquery query) {
        QueryRootNode clone = new QueryRootNode(engine, this.root);
        for (QueryObjTypeNode qotn : this.queryObjTypeNodeMap.values()) {
            clone.addQueryObjTypeNode(qotn.clone(engine, query));
        }
        clone.initialFactObjTypeNode = this.initialFactObjTypeNode;
        return clone;
    }
}
