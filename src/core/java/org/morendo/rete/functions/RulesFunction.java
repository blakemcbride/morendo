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
 */
package org.morendo.rete.functions;

import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;
import org.morendo.rule.Rule;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Peter Lin
 *     <p>The purpose of the function is to print out the names of the rules and the comment.
 */
public class RulesFunction implements Function {

    /** */
    public static final String RULES = "rules";

    public static final String LISTRULES = "list-defrules";

    public RulesFunction() {
        super();
    }

    public String getName() {
        return LISTRULES;
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Collection<?> rules = engine.getCurrentFocus().getAllRules();
        int count = 0;
        Iterator<?> itr = rules.iterator();
        while (itr.hasNext()) {
            Rule r = (Rule) itr.next();
            String name = r.getName();
            if (r instanceof org.morendo.rule.Defrule d && d.getOrGroup() != null) {
                // the rules an or group expanded into are listed once, under the written name
                if (d.getOrIndex() > 1) {
                    continue;
                }
                name = d.getOrGroup() + " (or)";
            }
            count++;
            engine.writeMessage(
                    name
                            + " \""
                            + r.getComment()
                            + "\" salience:"
                            + r.getSalience()
                            + " version:"
                            + r.getVersion()
                            + " no-agenda:"
                            + r.getNoAgenda()
                            + " temporal-activation:"
                            + r.isTemporalActivation()
                            + " cost-value:"
                            + r.getCostValue()
                            + "\r\n",
                    "t");
        }
        engine.writeMessage("for a total of " + count + "\r\n", "t");
        DefaultReturnVector rv = new DefaultReturnVector();
        return rv;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(list-defrules)";
    }
}
