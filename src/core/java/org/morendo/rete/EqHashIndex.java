/*
 * Copyright 2002-2008 Peter Lin
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

/**
 * @author Peter Lin
 *     <p>EqHashIndex is used by the BetaNode for indexing the facts that enter from the right.
 */
public class EqHashIndex implements HashIndex {

    /** */
    private Object[] values = null;

    private int hashCode;

    /** */
    public EqHashIndex(Object[] thevalues) {
        super();
        this.values = thevalues;
        calculateHash();
    }

    /** This is a very simple implementation that gets the slot hash from the deffact. */
    private void calculateHash() {
        if (this.values != null && this.values.length > 0) {
            for (int idx = 0; idx < values.length; idx++) {
                if (values[idx] != null) {
                    this.hashCode += values[idx].hashCode();
                }
            }
        }
    }

    public void clear() {
        this.values = null;
    }

    /** The method returns true if all values are equal */
    public boolean equals(Object val) {
        if (this == val) {
            return true;
        }
        EqHashIndex eval = (EqHashIndex) val;
        boolean eq = true;
        for (int idx = 0; idx < values.length; idx++) {
            if (!eval.values[idx].equals(this.values[idx])) {
                eq = false;
                break;
            }
        }
        return eq;
    }

    /**
     * Returns true if all values are not equal, by the rules of Evaluate.evaluateNotEqual (so "nil"
     * differs from any value except nil, and numbers compare numerically), with this index as the
     * left side and the given one as the right side, as in Binding.evaluate.
     *
     * @param val
     * @return
     */
    public boolean notEquals(Object val) {
        if (this == val) {
            return false;
        }
        EqHashIndex eval = (EqHashIndex) val;
        for (int idx = 0; idx < values.length; idx++) {
            if (!Evaluate.evaluateNotEqual(this.values[idx], eval.values[idx])) {
                return false;
            }
        }
        return true;
    }

    /** Method simply returns the cached hashCode. */
    public int hashCode() {
        return this.hashCode;
    }

    public String toPPString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i].toString());
        }
        return buf.toString();
    }
}
