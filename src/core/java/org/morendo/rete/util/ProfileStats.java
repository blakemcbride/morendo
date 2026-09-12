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
package org.morendo.rete.util;

/**
 * The profiling counters of one engine: how long assertions, retractions, agenda changes, firing
 * and cube queries took, and how many activations came and went. Each Rete owns an instance
 * (Rete.getProfileStats()), so two engines profiling at once do not mix their numbers.
 */
public class ProfileStats {

    public long assertTime = 0;
    public long retractTime = 0;
    public long rmActivation = 0;
    public long addActivation = 0;
    public int addcount = 0;
    public int rmcount = 0;
    public long fireTime = 0;
    public long averageCubeQueryTime = 0;
    public long indexTime = 0;

    protected long fstart = 0;
    protected long fend = 0;
    protected long assertstart = 0;
    protected long assertend = 0;
    protected long retractstart = 0;
    protected long retractend = 0;
    protected long addstart = 0;
    protected long addend = 0;
    protected long rmstart = 0;
    protected long rmend = 0;
    protected long cubeQueryStart = 0;
    protected long cubeQueryEnd = 0;
    protected long queryCount = 0;
    protected long cubeQueryTotal = 0;
    protected long indexStart = 0;
    protected long indexEnd = 0;

    public ProfileStats() {
        super();
    }

    public void resetStats() {
        assertTime = 0;
        retractTime = 0;
        rmActivation = 0;
        addActivation = 0;
        fireTime = 0;
    }

    public void startFire() {
        fstart = System.currentTimeMillis();
    }

    public void endFire() {
        fend = System.currentTimeMillis();
        if (fstart > 0) {
            addFireET(fend - fstart);
        }
    }

    public void addFireET(long time) {
        fireTime += time;
    }

    public void startAssert() {
        assertstart = System.currentTimeMillis();
    }

    public void endAssert() {
        assertend = System.currentTimeMillis();
        if (assertstart > 0) {
            addAssertET(assertend - assertstart);
        }
    }

    public void addAssertET(long time) {
        assertTime += time;
    }

    public void startRetract() {
        retractstart = System.currentTimeMillis();
    }

    public void endRetract() {
        retractend = System.currentTimeMillis();
        if (retractstart > 0) {
            addRetractET(retractend - retractstart);
        }
    }

    public void addRetractET(long time) {
        retractTime += time;
    }

    public void startAddActivation() {
        addstart = System.currentTimeMillis();
    }

    public void endAddActivation() {
        addend = System.currentTimeMillis();
        if (addstart > 0) {
            addAddActivationET(addend - addstart);
            addcount++;
        }
    }

    public void addAddActivationET(long time) {
        addActivation += time;
    }

    public void startRemoveActivation() {
        rmstart = System.currentTimeMillis();
    }

    public void endRemoveActivation() {
        rmend = System.currentTimeMillis();
        if (rmstart > 0) {
            addRemoveActivationET(rmend - rmstart);
            rmcount++;
        }
    }

    public void addRemoveActivationET(long time) {
        rmActivation += time;
    }

    public void startCubeQuery() {
        cubeQueryStart = System.currentTimeMillis();
    }

    public void endCubeQuery() {
        cubeQueryEnd = System.currentTimeMillis();
        queryCount++;
        calculateAverageCubeQuery();
    }

    public void calculateAverageCubeQuery() {
        cubeQueryTotal += cubeQueryEnd - cubeQueryStart;
        averageCubeQueryTime = cubeQueryTotal / queryCount;
    }

    public void startCubeIndex() {
        indexStart = System.currentTimeMillis();
    }

    public void endCubeIndex() {
        indexEnd = System.currentTimeMillis();
        indexTime += indexEnd - indexStart;
    }

    /** Clears every counter. */
    public void reset() {
        assertTime = 0;
        retractTime = 0;
        rmActivation = 0;
        addActivation = 0;
        fireTime = 0;
        fstart = 0;
        fend = 0;
        assertstart = 0;
        assertend = 0;
        retractstart = 0;
        retractend = 0;
        addstart = 0;
        addend = 0;
        rmstart = 0;
        rmend = 0;
        addcount = 0;
        rmcount = 0;
        cubeQueryStart = 0;
        cubeQueryEnd = 0;
        averageCubeQueryTime = 0;
        queryCount = 0;
        cubeQueryTotal = 0;
        indexTime = 0;
    }
}
