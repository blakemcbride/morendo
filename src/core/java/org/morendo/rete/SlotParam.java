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
 */
public final class SlotParam extends AbstractParam {

    /** */
    protected ValueType valueType = ValueType.SLOT;

    protected Slot slot = null;

    /**
     * @param type
     * @param slot
     */
    public SlotParam(Slot slot) {
        super();
        this.slot = slot;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.ReturnValue#getValueType()
     */
    public ValueType getValueType() {
        return this.valueType;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.ReturnValue#getValue()
     */
    public Object getValue() {
        return this.slot;
    }

    public Slot getSlotValue() {
        return this.slot;
    }

    /**
     * Slot parameter is only used internally, so normal user functions should not need to deal with
     * slot parameters.
     */
    public Object getValue(Rete engine, ValueType valueType) {
        return this.slot;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rete.Parameter#reset()
     */
    public void reset() {
        this.slot = null;
    }
}
