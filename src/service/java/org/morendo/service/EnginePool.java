/*
 * Copyright 2026 Blake McBride
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
package org.morendo.service;

import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * The engines of one rule application. Engines are checked out by request threads and checked in
 * when the request is done; the pool grows on demand up to the application's maximum, and a
 * check-out on an exhausted pool waits for an engine to come back. Safe to use from many threads.
 */
public final class EnginePool {

    private final RuleApplication application;
    private final LinkedBlockingDeque<Rete> idle = new LinkedBlockingDeque<>();
    private final Object growth = new Object();
    private int created = 0;

    public EnginePool(RuleApplication application) {
        this.application = application;
    }

    public RuleApplication getApplication() {
        return this.application;
    }

    /** Creates the application's initial engines. */
    public void fill() {
        for (int c = 0; c < this.application.getInitialPool(); c++) {
            Rete engine = create();
            if (engine != null) {
                this.idle.addLast(engine);
            }
        }
    }

    private Rete create() {
        synchronized (this.growth) {
            if (this.created >= this.application.getMaxPool()) {
                return null;
            }
            Rete engine = new Rete();
            this.application.initializeEngine(engine);
            this.created++;
            this.application.setCurrentPoolCount(this.created);
            return engine;
        }
    }

    /**
     * An idle engine, a new one while the pool is below its maximum, or one that comes back within
     * the timeout; null when none did. A timeout of zero does not wait.
     */
    public Rete checkOut(long timeoutMillis) throws InterruptedException {
        Rete engine = this.idle.pollFirst();
        if (engine != null) {
            return engine;
        }
        engine = create();
        if (engine != null) {
            return engine;
        }
        return timeoutMillis <= 0
                ? null
                : this.idle.pollFirst(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /** Returns an engine to the pool. */
    public void checkIn(Rete engine) {
        this.idle.addLast(engine);
    }

    /** The engines waiting in the pool. */
    public int idleCount() {
        return this.idle.size();
    }

    /** The engines created so far, checked out or not. */
    public int createdCount() {
        synchronized (this.growth) {
            return this.created;
        }
    }

    /** A copy of the idle engines, for administration views. */
    public List<Rete> snapshot() {
        return new ArrayList<>(this.idle);
    }

    /**
     * Applies an operation to every idle engine, taking them out of the pool while it runs and
     * putting them back after; stops at the first that answers false. Engines checked out at the
     * time are not visited.
     */
    public boolean forEachIdle(Predicate<Rete> operation) {
        List<Rete> engines = new ArrayList<>();
        this.idle.drainTo(engines);
        boolean ok = true;
        try {
            for (Rete engine : engines) {
                if (ok) {
                    ok = operation.test(engine);
                }
            }
        } finally {
            for (Rete engine : engines) {
                this.idle.addLast(engine);
            }
        }
        return ok;
    }

    /** Closes the idle engines and forgets them; engines checked out are the holder's to close. */
    public void closeAll() {
        List<Rete> engines = new ArrayList<>();
        this.idle.drainTo(engines);
        for (Rete engine : engines) {
            engine.close();
        }
        synchronized (this.growth) {
            this.created = 0;
            this.application.setCurrentPoolCount(0);
        }
    }
}
