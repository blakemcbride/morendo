/*
 * Copyright 2002-2009 Jamocha
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

import java.util.Iterator;
import java.util.Map;

/**
 * @author Peter Lin
 *     <p>Basic implementation of Alpha memory. It uses HashMap for storing the indexes.
 */
public class HashedAlphaMemoryImpl {

    /** */

    /** index -> (fact -> fact); HashedNeqAlphaMemory nests a second map level in the values. */
    protected Map<HashIndex, Map<Object, Object>> memory = null;

    protected int counter = 0;

    /** */
    public HashedAlphaMemoryImpl(String name, Rete engine) {
        super();
        memory = engine.newAlphaMemoryMap(name);
    }

    /** addPartialMatch stores the fact with the factId as the key. */
    public int addPartialMatch(HashIndex index, Fact fact, Rete engine) {
        Map<Object, Object> matches = this.memory.get(index);
        int count = 0;
        if (matches == null) {
            count = this.addNewPartialMatch(index, fact, engine);
        } else {
            matches.put(fact, fact);
            count = matches.size();
        }
        this.counter++;
        return count;
    }

    public int addNewPartialMatch(HashIndex index, Fact fact, Rete engine) {
        Map<Object, Object> matches = engine.newMap();
        matches.put(fact, fact);
        this.memory.put(index, matches);
        return 1;
    }

    /** clear the memory. */
    public void clear() {
        for (Map<Object, Object> matches : this.memory.values()) {
            matches.clear();
        }
        this.memory.clear();
    }

    public boolean isPartialMatch(HashIndex index, Fact fact) {
        Map<Object, Object> list = this.memory.get(index);
        if (list != null) {
            return list.containsKey(fact);
        } else {
            return false;
        }
    }

    /** remove a partial match from the memory */
    public int removePartialMatch(HashIndex index, Fact fact) {
        Map<Object, Object> list = this.memory.get(index);
        if (list != null) {
            list.remove(fact);
            if (list.size() == 0) {
                this.memory.remove(index);
            }
            this.counter--;
            return list.size();
        } else {
            return 0;
        }
    }

    /** Return the number of memories of all hash buckets */
    public int size() {
        int count = 0;
        for (Map<Object, Object> matches : this.memory.values()) {
            count += matches.size();
        }
        return count;
    }

    public int bucketCount() {
        return this.counter;
    }

    /** Return an iterator of the values */
    public Iterator<Object> iterator(HashIndex index) {
        Map<Object, Object> list = this.memory.get(index);
        if (list != null) {
            return list.values().iterator();
        } else {
            return null;
        }
    }

    public int count(HashIndex index) {
        Map<Object, Object> list = this.memory.get(index);
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    /**
     * return an arraylist with all the facts
     *
     * @return
     */
    public Object[] iterateAll() {
        Object[] all = new Object[this.counter];
        int idx = 0;
        for (Map<Object, Object> f : this.memory.values()) {
            for (Object fact : f.values()) {
                all[idx] = fact;
                idx++;
            }
        }
        return all;
    }

    public Iterator<HashIndex> iterateIndexKeys() {
        return this.memory.keySet().iterator();
    }
}
