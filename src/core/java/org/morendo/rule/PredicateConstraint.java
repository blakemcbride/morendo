/*
 * Copyright 2002-2006 Peter Lin
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
package org.morendo.rule;

import org.morendo.rete.BoundParam;
import org.morendo.rete.Constants;
import org.morendo.rete.ConversionUtils;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Parameter;
import org.morendo.rete.ValueParam;

import java.util.ArrayList;
// import java.util.Iterator;
import java.util.List;

/**
 * @author Peter Lin
 *     <p>Predicate constraint binds the slot and then performs some function on it. For example
 *     (myslot ?s&:(> ?s 100) )
 */
public final class PredicateConstraint implements Constraint {

    /** */

    /** the name of the slot */
    protected String name = null;

    /** the name of the variable */
    protected String varName = null;

    /** the name of the function */
    protected String functionName = null;

    protected Object value = null;

    protected ArrayList<Object> parameters =
            new ArrayList<>(); // Should be Parameter, but needs re-factor parser

    protected boolean isPredicateJoin = false;

    protected boolean reverseOperator = false;

    /** */
    public PredicateConstraint() {
        super();
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rule.Constraint#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rule.Constraint#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rule.Constraint#getValue()
     */
    public Object getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see woolfel.engine.rule.Constraint#setValue(java.lang.Object)
     */
    public void setValue(Object val) {
        this.value = val;
    }

    public String getVariableName() {
        return this.varName;
    }

    public void setVariableName(String name) {
        this.varName = name;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public void setFunctionName(String func) {
        this.functionName = func;
    }

    public boolean isPredicateJoin() {
        return this.isPredicateJoin;
    }

    public void addParameters(List<Object> params) {
        this.parameters.addAll(params);
        int bcount = 0;
        // we try to set the value
        for (int idx = 0; idx < parameters.size(); idx++) {
            Object p = parameters.get(idx);
            // for now, a simple implementation
            if (p instanceof ValueParam valueParam) {
                this.setValue((valueParam).getValue());
                if (idx == 0) {
                    this.reverseOperator = true;
                }
            } else if (p instanceof BoundParam bp) {
                if (!bp.getVariableName().equals(this.varName)) {
                    this.setValue(p);
                }
                bcount++;
            } else if (p instanceof FunctionParam2 fparam) {
                if (fparam.hasBoundParameter()) {
                    bcount++;
                }
            }
        }
        if (bcount > 1) {
            this.isPredicateJoin = true;
        }
    }

    public void addParameter(Parameter param) {
        this.parameters.add(param);
        if (param instanceof ValueParam valueParamValue) {
            this.setValue((valueParamValue).getValue());
        } else if (param instanceof BoundParam boundParam && this.varName == null) {
            this.varName = (boundParam).getVariableName();
        }
    }

    public List<?> getParameters() {
        return this.parameters;
    }

    public int parameterCount() {
        return this.parameters.size();
    }

    public boolean reverseOperator() {
        return this.reverseOperator;
    }

    /**
     * the purpose of normalize is to look at the order of the parameters and flip the operator if
     * necessary
     */
    public void normalize() {}

    public String toPPString() {
        String function = this.functionName;
        if (this.reverseOperator) {
            function = ConversionUtils.getOppositeOperator(function);
        }
        if (this.value instanceof BoundParam) {
            return "    ("
                    + this.name
                    + " ?"
                    + this.varName
                    + "&:("
                    + function
                    + " ?"
                    + this.varName
                    + " "
                    + ((BoundParam) this.value).toPPString()
                    + ") )"
                    + Constants.LINEBREAK;
        } else if (this.value != null) {
            return "    ("
                    + this.name
                    + " ?"
                    + this.varName
                    + "&:("
                    + function
                    + " ?"
                    + this.varName
                    + " "
                    + this.value.toString()
                    + ") )"
                    + Constants.LINEBREAK;
        } else {
            StringBuilder buf = new StringBuilder();
            if (this.parameters != null && this.parameters.size() > 0) {
                buf.append("    (" + this.name + " ?" + this.varName + "&:(" + function + " ");
                for (int idx = 0; idx < this.parameters.size(); idx++) {
                    Parameter p = (Parameter) parameters.get(idx);
                    if (p instanceof FunctionParam2 functionParam2) {
                        buf.append((functionParam2).toPPString());
                    }
                }
                buf.append(" ) )" + Constants.LINEBREAK);
            }
            return buf.toString();
        }
    }

    //	public void addParameters(List<Object> params) {
    // TODO Auto-generated method stub

    //	}
}
