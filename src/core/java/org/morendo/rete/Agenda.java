/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://jamocha.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete;

import org.morendo.rule.Rule;

import java.util.Map;

/**
 * @author Peter Lin
 *     <p>The design of the agenda is based on CLIPS, which uses modules to contain different
 *     rulesets. When a new activation is added to the agenda, it is added to a specific module. By
 *     default, the rule engine creates a main module. If no additional modules are created, all
 *     activations are added to the main module. If there are multiple modules, the activation is
 *     added to the activation list of that given module. Only the activations of the current module
 *     will be fired.
 */
public class Agenda {

    /** */

    /** The ArrayList for the modules. */
    protected Map<?, ?> modules = null;

    private Rete engine = null;

    private boolean watch = false;

    private boolean profAdd = false;

    private boolean profRm = false;

    private boolean startReset = false;

    /** The agenda takes an instance of Rete. the agenda needs a handle to the engine to do work. */
    public Agenda(Rete engine) {
        super();
        this.engine = engine;
        this.modules = engine.newLocalMap();
    }

    public void setWatch(boolean w) {
        this.watch = w;
    }

    public boolean watch() {
        return this.watch;
    }

    public void setProfileAdd(boolean prof) {
        this.profAdd = prof;
    }

    public boolean profileAdd() {
        return this.profAdd;
    }

    public void setProfileRemove(boolean prof) {
        this.profRm = prof;
    }

    public boolean profileRemove() {
        return this.profRm;
    }

    /**
     * Add an activation to the agenda. If the engine is current being reset, no activations are
     * added
     *
     * @param actv
     */
    public void addActivation(Activation actv) {
        if (!this.startReset) {
            // the implementation should get the current focus from Rete
            // and then add the activation to the Module.
            if (profAdd) {
                addActivationWProfile(actv);
            } else {
                if (watch) {
                    engine.writeMessage("=> " + actv.toPPString() + Constants.LINEBREAK, "t");
                }
                actv.getRule().getModule().addActivation(actv);
            }
            autoFocus(actv);
        }
    }

    /** A rule declared auto-focus brings its module into focus when it activates. */
    private void autoFocus(Activation actv) {
        Rule rule = actv.getRule();
        if (rule.getAutoFocus() && rule.getModule() != null) {
            WorkingMemory wm = engine.getWorkingMemory();
            if (wm.getCurrentFocus() != rule.getModule()) {
                wm.pushFocus(rule.getModule());
            }
        }
    }

    /**
     * if profiling is turned on, the method is called to add new activations to the agenda
     *
     * @param actv
     */
    public void addActivationWProfile(Activation actv) {
        engine.getProfileStats().startAddActivation();
        actv.getRule().getModule().addActivation(actv);
        engine.getProfileStats().endAddActivation();
    }

    /**
     * Method is called to remove an activation from the agenda.
     *
     * @param actv
     */
    public void removeActivation(Activation actv) {
        if (profRm) {
            removeActivationWProfile(actv);
        } else {
            if (watch) {
                engine.writeMessage("<= " + actv.toPPString() + Constants.LINEBREAK, "t");
            }
            actv.getRule().getModule().removeActivation(actv);
        }
    }

    /**
     * if the profiling is turned on for remove, the method is called to remove activations.
     *
     * @param actv
     */
    public void removeActivationWProfile(Activation actv) {
        engine.getProfileStats().startRemoveActivation();
        actv.getRule().getModule().removeActivation(actv);
        engine.getProfileStats().endRemoveActivation();
    }

    /** Clear will clear all the modules and remove all activations */
    public void clear() {
        for (Object key : this.modules.keySet()) {
            Module mod = (Module) this.modules.get(key);
            mod.clear();
        }
        this.modules.clear();
    }

    public void startReset() {
        this.startReset = true;
    }

    public void endReset() {
        this.startReset = false;
    }
}
