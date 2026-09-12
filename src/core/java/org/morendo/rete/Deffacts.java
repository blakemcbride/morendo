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
package org.morendo.rete;

import org.morendo.rete.exception.AssertException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named list of facts that {@code (reset)} asserts, the CLIPS {@code deffacts} construct. Each
 * entry is what the parser produces for a fact in a data file: the template name and the slots.
 */
public final class Deffacts {

    private final String name;
    private final String comment;
    private final List<ValueParam[]> facts;

    public Deffacts(String name, String comment, List<ValueParam[]> facts) {
        this.name = name;
        this.comment = comment == null ? "" : comment;
        this.facts = new ArrayList<>(facts);
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    /** The facts, each as the template name followed by the slot array. */
    public List<ValueParam[]> getFacts() {
        return Collections.unmodifiableList(this.facts);
    }

    /**
     * Asserts every fact of the list into the engine and returns how many were asserted. Slot
     * values that are calls or global variables are evaluated at this point.
     */
    public int assertFacts(Rete engine) throws AssertException {
        int count = 0;
        for (ValueParam[] entry : this.facts) {
            String template = entry[0].getStringValue();
            Deftemplate tmpl = (Deftemplate) engine.findTemplate(template);
            if (tmpl == null) {
                throw new AssertException(
                        "deffacts " + this.name + ": unknown template " + template);
            }
            Deffact fact = (Deffact) tmpl.createFact((Object[]) entry[1].getValue(), -1);
            if (fact.hasBinding()) {
                fact.resolveValues(engine, null);
                fact = fact.cloneFact();
            }
            engine.assertFact(fact);
            count++;
        }
        return count;
    }

    /** The construct as it could be read back. */
    public String toPPString() {
        StringBuilder buf = new StringBuilder();
        buf.append("(deffacts ").append(this.name);
        if (!this.comment.isEmpty()) {
            buf.append(" \"").append(this.comment).append('"');
        }
        for (ValueParam[] entry : this.facts) {
            buf.append(Constants.LINEBREAK).append("  (").append(entry[0].getStringValue());
            for (Object slot : (Object[]) entry[1].getValue()) {
                if (slot instanceof MultiSlot ms) {
                    buf.append(" (").append(ms.getName());
                    for (Object v : ms.getValue()) {
                        buf.append(' ').append(valueText(v));
                    }
                    buf.append(')');
                } else if (slot instanceof Slot s) {
                    buf.append(" (")
                            .append(s.getName())
                            .append(' ')
                            .append(valueText(s.getValue()));
                    buf.append(')');
                }
            }
            buf.append(')');
        }
        buf.append(')');
        return buf.toString();
    }

    private static String valueText(Object value) {
        if (value instanceof String s) {
            return '"' + s + '"';
        } else if (value instanceof BoundParam bp) {
            return "?" + bp.getVariableName();
        } else if (value instanceof FunctionParam2 fp) {
            return fp.toPPString();
        } else if (value == null) {
            return "nil";
        }
        return value.toString();
    }
}
