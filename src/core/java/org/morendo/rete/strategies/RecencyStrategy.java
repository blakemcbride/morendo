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
package org.morendo.rete.strategies;

import org.morendo.rete.Activation;
import org.morendo.rete.ActivationList;
import org.morendo.rete.Fact;
import org.morendo.rete.Strategy;

/**
 * @author Peter Lin
 */
public class RecencyStrategy implements Strategy {

    /** */

    /** */
    public RecencyStrategy() {
        super();
    }

    public String getName() {
        return "recency";
    }

    /** Current implementation will check which order the list is and call the appropriate method */
    public void addActivation(ActivationList thelist, Activation newActivation) {
        thelist.addActivation(newActivation);
    }

    /** Current implementation will check which order the list is and call the appropriate method */
    public Activation nextActivation(ActivationList thelist) {
        return thelist.nextActivation();
    }

    /**
     * The method first compares the salience. If the salience is equal, we then compare the
     * aggregate time.
     *
     * @param left
     * @param right
     * @return
     */
    public int compare(Activation left, Activation right) {
        if (right != null) {
            if (left.getRule().getSalience() == right.getRule().getSalience()) {
                // we compare the facts based on how recent it is
                return compareRecency(left, right);
            } else {
                if (left.getRule().getSalience() > right.getRule().getSalience()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        } else {
            return 1;
        }
    }

    /**
     * compare will look to see which activation has more facts. it will first compare the timestamp
     * of the facts. If the facts are equal, it will return the activation with more facts.
     *
     * @param left
     * @param right
     * @return
     */
    protected int compareRecency(Activation left, Activation right) {
        Fact[] lfacts = left.getFacts();
        Fact[] rfacts = right.getFacts();
        int len = lfacts.length;
        if (rfacts.length < len) {
            len = rfacts.length;
        }
        // first we compare the time stamp
        for (int idx = 0; idx < len; idx++) {
            if (lfacts[idx].timeStamp() > rfacts[idx].timeStamp()) {
                return 1;
            } else if (lfacts[idx].timeStamp() < rfacts[idx].timeStamp()) {
                return -1;
            }
        }
        // the activation with more facts has a higher priority
        if (lfacts.length > rfacts.length) {
            return 1;
        } else if (lfacts.length < rfacts.length) {
            return -1;
        }
        // next we compare the fact id
        for (int idx = 0; idx < len; idx++) {
            if (lfacts[idx].getFactId() > rfacts[idx].getFactId()) {
                return 1;
            } else if (lfacts[idx].getFactId() < rfacts[idx].getFactId()) {
                return -1;
            }
        }
        return 0;
    }
}
