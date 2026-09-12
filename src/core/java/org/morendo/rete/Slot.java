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

/**
 * @author Peter Lin
 *     <p>Slot is similar to CLIPS slots, though slightly different. A slot has a name and object
 *     value. Slot can inspect the type of the value.
 */
public class Slot extends BaseSlot {

    /** */
    private static final long serialVersionUID = 1L;

    public Slot() {}

    /**
     * Create a new instance with a given name
     *
     * @param name
     */
    public Slot(String name) {
        this.name = name;
    }

    /** For convenience you can create here a slot with a given value directly */
    public Slot(String name, Object value) {
        this(name);
        this.value = value;
    }

    /**
     * get the value of the slot
     *
     * @return
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * set the value of the slot
     *
     * @param val
     */
    public void setValue(Object val) {
        this.value = val;
        if (this.getValueType() == null) {
            inspectType();
        }
    }

    /** method will look at the value and set the ValueType type */
    protected void inspectType() {
        if (this.value instanceof Double) {
            this.setValueType(ValueType.DOUBLE_PRIM);
        } else if (this.value instanceof Long) {
            this.setValueType(ValueType.LONG_PRIM);
        } else if (this.value instanceof Float) {
            this.setValueType(ValueType.FLOAT_PRIM);
        } else if (this.value instanceof Short) {
            this.setValueType(ValueType.SHORT_PRIM);
        } else if (this.value instanceof Integer) {
            this.setValueType(ValueType.INT_PRIM);
        } else {
            this.setValueType(ValueType.OBJECT);
        }
    }

    /** A convienance method to clone slots */
    public Object clone() {
        Slot newslot = new Slot();
        newslot.setId(this.getId());
        newslot.setName(this.getName());
        newslot.value = this.value;
        newslot.setValueType(this.getValueType());
        return newslot;
    }

    public String valueToString() {
        if (this.getValueType() == ValueType.STRING) {
            return "\"" + this.value.toString() + "\"";
        } else {
            return this.value.toString();
        }
    }
}
