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

import org.morendo.rule.Query;
import org.morendo.rule.Rule;

/**
 * @author Peter Lin
 *     <p>Describe difference between the Function parameters
 */
public final class FunctionParam2 extends AbstractParam {

    /** */
    protected Function func = null;

    protected String funcName = null;
    private Parameter[] params = null;
    private Rete engine = null;
    protected Fact[] facts;

    public FunctionParam2() {
        super();
    }

    public void setFunctionName(String name) {
        this.funcName = name;
    }

    public String getFunctionName() {
        return this.funcName;
    }

    public void setEngine(Rete engine) {
        this.engine = engine;
    }

    public void configure(Rete engine, Rule util) {
        if (this.engine == null) {
            this.engine = engine;
        }
        for (int idx = 0; idx < this.params.length; idx++) {
            if (this.params[idx] instanceof BoundParam) {
                // we need to set the row value if the binding is a slot or fact
                BoundParam bp = (BoundParam) this.params[idx];
                Binding b1 = util.getBinding(bp.getVariableName());
                if (b1 != null) {
                    bp.setRow(b1.getLeftRow());
                    bp.setColumn(b1.getLeftIndex());
                    if (b1.getLeftIndex() == -1) {
                        bp.setObjectBinding(true);
                    }
                }
            } else if (this.params[idx] instanceof FunctionParam2 nested) {
                // a call nested deeper: its variables need their rows as well
                nested.configure(engine, util);
            }
        }
    }

    public void configure(Rete engine, Query util) {
        if (this.engine == null) {
            this.engine = engine;
        }
        for (int idx = 0; idx < this.params.length; idx++) {
            if (this.params[idx] instanceof BoundParam) {
                // we need to set the row value if the binding is a slot or fact
                BoundParam bp = (BoundParam) this.params[idx];
                Binding b1 = util.getBinding(bp.getVariableName());
                if (b1 != null) {
                    bp.setRow(b1.getLeftRow());
                    bp.setColumn(b1.getLeftIndex());
                    if (b1.getLeftIndex() == -1) {
                        bp.setObjectBinding(true);
                    }
                }
            } else if (this.params[idx] instanceof FunctionParam2 nested) {
                // a call nested deeper: its variables need their rows as well
                nested.configure(engine, util);
            }
        }
    }

    public void setParameters(Parameter[] params) {
        this.params = params;
    }

    public Parameter[] getParameters() {
        return this.params;
    }

    public boolean hasBoundParameter() {
        for (int idx = 0; idx < params.length; idx++) {
            if (params[idx] instanceof BoundParam) {
                return true;
            }
        }
        return false;
    }

    public void lookUpFunction() {
        this.func = engine.findFunction(this.funcName);
    }

    public ValueType getValueType() {
        return this.func.getReturnType();
    }

    /** The function to call; a call to a name the engine does not know is an error. */
    private Function require() {
        if (this.func == null) {
            throw new IllegalArgumentException("unknown function: " + this.funcName);
        }
        return this.func;
    }

    public Object getValue() {
        if (this.params != null) {
            if (this.func == null && this.engine != null) {
                lookUpFunction();
            }
            this.setFact();
            return require().executeFunction(engine, this.params);
        } else {
            return null;
        }
    }

    protected void setFact() {
        for (int idx = 0; idx < this.params.length; idx++) {
            if (this.params[idx] instanceof BoundParam bp) {
                bp.setFact(this.facts);
                if (bp.getFact() == null && this.engine != null) {
                    bp.resolveBinding(this.engine);
                }
            } else if (this.params[idx] instanceof FunctionParam) {
                ((FunctionParam) this.params[idx]).setFacts(this.facts);
            } else if (this.params[idx] instanceof FunctionParam2 nested && this.facts != null) {
                nested.setFacts(this.facts);
            }
        }
    }

    /**
     * TODO we may want to check the value type and throw and exception for now just getting it to
     * work.
     */
    public Object getValue(Rete engine, ValueType valueType) {
        if (this.params != null) {
            this.engine = engine;
            lookUpFunction();
            checkParameters();
            ReturnVector rval = require().executeFunction(engine, this.params);
            if (valueType == ValueType.OBJECT_RETURN) {
                return rval;
            } else if (valueType == ValueType.BIG_DECIMAL) {
                return rval.firstReturnValue().getBigDecimalValue();
            } else if (valueType == ValueType.OBJECT || valueType == ValueType.ARRAY) {
                return rval.firstReturnValue().getValue();
            } else if (valueType == ValueType.INTEGER_OBJECT || valueType == ValueType.INT_PRIM) {
                return rval.firstReturnValue().getIntValue();
            } else if (valueType == ValueType.LONG_OBJECT || valueType == ValueType.LONG_PRIM) {
                return rval.firstReturnValue().getLongValue();
            } else if (valueType == ValueType.FLOAT_OBJECT || valueType == ValueType.FLOAT_PRIM) {
                return rval.firstReturnValue().getFloatValue();
            } else if (valueType == ValueType.DOUBLE_OBJECT || valueType == ValueType.DOUBLE_PRIM) {
                return rval.firstReturnValue().getDoubleValue();
            } else {
                return rval.firstReturnValue().getValue();
            }
        } else {
            return null;
        }
    }

    protected void checkParameters() {
        for (int idx = 0; idx < this.params.length; idx++) {
            if (params[idx] instanceof BoundParam bp) {
                bp.setFact(this.facts);
                if (bp.getFact() == null) {
                    // not a pattern variable: a variable of the enclosing scope or a global
                    bp.resolveBinding(this.engine);
                }
            } else if (params[idx] instanceof FunctionParam2 nested && this.facts != null) {
                nested.setFacts(this.facts);
            }
        }
    }

    public void reset() {
        this.engine = null;
        this.params = null;
    }

    public String toPPString() {
        this.lookUpFunction();
        return this.func.toPPString(this.params, 1);
    }

    public Fact[] getFacts() {
        return facts;
    }

    public void setFacts(Fact[] facts) {
        this.facts = facts;
        if (this.params != null) {
            for (Parameter param : this.params) {
                if (param instanceof FunctionParam2 nested) {
                    nested.setFacts(facts);
                }
            }
        }
    }

    /**
     * Evaluates the call with the engine and facts it was given and returns the first value of the
     * result, or null when the call answers nothing.
     */
    public Object evaluate() {
        if (this.engine == null) {
            return null;
        }
        ReturnVector rv = (ReturnVector) getValue(this.engine, ValueType.OBJECT_RETURN);
        return rv == null || rv.size() == 0 ? null : rv.firstReturnValue().getValue();
    }

    public FunctionParam2 clone() {
        Parameter[] cloneParams = new Parameter[this.params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof BoundParam) {
                BoundParam bp = (BoundParam) params[i];
                cloneParams[i] = bp.clone();
            } else if (params[i] instanceof ValueParam) {
                ValueParam vp = (ValueParam) params[i];
                cloneParams[i] = vp.cloneParameter();
            } else if (params[i] instanceof FunctionParam) {
                FunctionParam fp = (FunctionParam) params[i];
                cloneParams[i] = fp.clone();
            } else if (params[i] instanceof FunctionParam2) {
                FunctionParam2 fp = (FunctionParam2) params[i];
                cloneParams[i] = fp.clone();
            }
        }
        FunctionParam2 clone = new FunctionParam2();
        clone.engine = this.engine;
        clone.facts = this.facts;
        clone.func = this.func;
        clone.funcName = this.funcName;
        clone.objBinding = this.objBinding;
        clone.params = cloneParams;
        return clone;
    }
}
