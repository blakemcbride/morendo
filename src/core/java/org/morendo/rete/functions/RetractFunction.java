/*
 * Copyright 2002-2010 Peter Lin
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
 * Modified by Dave Woodman - 21/6/21 to permit
 * (bind ?fred (assert ....
 * (retract ?Fred)
 *
 *
 */
package org.morendo.rete.functions;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Deffact;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.exception.RetractException;

import java.math.BigDecimal;

public class RetractFunction implements Function {

    /** */
    public static final String RETRACT = "retract";

    public RetractFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector rv = new DefaultReturnVector();
        if (params != null && params.length >= 1) {
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof BoundParam) {
                    BoundParam bp = (BoundParam) params[idx];
                    Deffact fact = (Deffact) bp.getFact();
                    try {
                        if (fact == null)
                            fact =
                                    (Deffact)
                                            engine.getFactById(
                                                    Long.parseLong(
                                                            engine.getBinding(bp.getVariableName())
                                                                    .toString()));
                        if (fact.getObjectInstance() != null)
                            engine.retractObject(fact.getObjectInstance());
                        else engine.retractFact(fact);
                        DefaultReturnValue rval =
                                new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.TRUE);
                        rv.addReturnValue(rval);
                    } catch (RetractException | NumberFormatException e) {
                        DefaultReturnValue rval =
                                new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE);
                        rv.addReturnValue(rval);
                    }
                } else if (params[idx] instanceof ValueParam) {
                    BigDecimal bi = params[idx].getBigDecimalValue();
                    try {
                        engine.retractById(bi.longValue());
                        DefaultReturnValue rval =
                                new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.TRUE);
                        rv.addReturnValue(rval);
                    } catch (RetractException e) {
                        DefaultReturnValue rval =
                                new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, Boolean.FALSE);
                        rv.addReturnValue(rval);
                    }
                }
            }
        }
        return rv;
    }

    public String getName() {
        return RETRACT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {BoundParam.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(retract [?binding|fact-id])\n"
                + "Function description:\n"
                + "\tAllows the user to remove facts from the fact-list.";
    }
}
