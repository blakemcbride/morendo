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
 *     <p>ShellBoundParam is meant for calling EchoFunction in the shell. It is different than
 *     BoundParam in a couple of ways. The first is that users can bind an object, fact or value.
 *     Bindings in the shell are global bindings.
 */
public final class ShellBoundParam extends AbstractParam {

    /** */
    /** the int value defining the valueType */
    protected ValueType valueType = null;

    protected String globalVarName = "";
    protected Object value = null;

    /** */
    public ShellBoundParam() {
        super();
    }

    /** Sets the variable name; a leading {@code ?} is dropped, as {@link BoundParam} does. */
    public void setDefglobalName(String name) {
        this.globalVarName = name.startsWith("?") ? name.substring(1) : name;
    }

    public String getDefglobalName() {
        return this.globalVarName;
    }

    public ValueType getValueType() {
        return this.valueType;
    }

    /**
     * The method needs to be called before getting the value. First we need to lookup the binding.
     *
     * @param engine
     */
    public void resolveBinding(Rete engine) {
        this.value = engine.getBinding(this.globalVarName);
    }

    /** The method returns the bound object */
    public Object getValue() {
        return this.value;
    }

    /** the class will resolve the variable with the engine */
    public Object getValue(Rete engine, ValueType valueType) {
        return this.value = engine.getBinding(this.globalVarName);
    }

    /** if the value was null, the method returns a message "defglobal not found". */
    public String getStringValue() {
        if (getValue() != null) {
            if (getValue() instanceof String) {
                return (String) getValue();
            } else if (getValue() instanceof Object[] list) {
                return java.util.Arrays.deepToString(list);
            } else {
                return getValue().toString();
            }
        } else {
            return "variable not bound";
        }
    }

    public void reset() {
        this.valueType = null;
        this.globalVarName = "";
    }
}
