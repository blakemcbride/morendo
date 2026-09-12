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
package org.morendo.parser.clips;

import org.morendo.rete.Rete;
import org.morendo.rete.functions.ShellFunction;
import org.morendo.rule.AndCondition;
import org.morendo.rule.Condition;
import org.morendo.rule.Defrule;
import org.morendo.rule.OrCondition;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Expands a rule whose left-hand side holds {@code (or ...)} groups into one rule per combination
 * of alternatives, the way CLIPS does. The rule's text is rebuilt from its tokens with one
 * alternative in place of each group and parsed again; a group nested inside an alternative is
 * expanded by that parse. The rules are named {@code name&1}, {@code name&2}, ... and remember
 * their group and the original text.
 */
public final class OrExpansion {

    private OrExpansion() {}

    /** True when the rule has an or group at the top level of its conditions. */
    public static boolean hasOrGroup(Defrule rule) {
        return !groups(rule).isEmpty();
    }

    /** The rule's text as written, from its first token to its last. */
    public static String sourceText(Token first, Token last) {
        return text(first, last, List.of(), new int[0]);
    }

    /**
     * The expanded rules. {@code first} is the {@code defrule} token and {@code last} the last
     * token of the rule body, before the closing parenthesis.
     */
    public static Defrule[] expand(Rete engine, Token first, Token last, Defrule rule)
            throws ParseException {
        List<OrCondition> groups = groups(rule);
        String source = sourceText(first, last);
        int combinations = 1;
        for (OrCondition group : groups) {
            combinations *= group.size();
        }
        List<Defrule> members = new ArrayList<>();
        for (int combo = 0; combo < combinations; combo++) {
            int[] choice = new int[groups.size()];
            int rest = combo;
            for (int g = groups.size() - 1; g >= 0; g--) {
                choice[g] = rest % groups.get(g).size();
                rest /= groups.get(g).size();
            }
            String variant = text(first, last, groups, choice);
            CLIPSParser parser =
                    new CLIPSParser(
                            engine,
                            new ByteArrayInputStream(variant.getBytes(StandardCharsets.UTF_8)));
            Object parsed = parser.basicExpr();
            Object value =
                    parsed instanceof ShellFunction fn && fn.getParameters().length == 1
                            ? fn.getParameters()[0].getValue()
                            : null;
            if (value instanceof Defrule[] nested) {
                members.addAll(List.of(nested));
            } else if (value instanceof Defrule member) {
                members.add(member);
            } else {
                throw new ParseException("cannot expand the or group of rule " + rule.getName());
            }
        }
        String base = rule.getName();
        Defrule[] result = new Defrule[members.size()];
        for (int i = 0; i < result.length; i++) {
            Defrule member = members.get(i);
            member.setName(base + "&" + (i + 1));
            member.setOrGroup(base, i + 1);
            member.setSourceText(source);
            result[i] = member;
        }
        return result;
    }

    private static List<OrCondition> groups(Defrule rule) {
        List<OrCondition> groups = new ArrayList<>();
        for (Condition ce : rule.getConditions()) {
            if (ce instanceof OrCondition or) {
                groups.add(or);
            } else if (ce instanceof AndCondition and) {
                for (Object nested : and.getNestedConditionalElement()) {
                    if (nested instanceof OrCondition or) {
                        groups.add(or);
                    }
                }
            }
        }
        return groups;
    }

    /** The rule text with the chosen alternative written in place of each group. */
    private static String text(Token first, Token last, List<OrCondition> groups, int[] choice) {
        StringBuilder buf = new StringBuilder("(");
        Token stop = last.next;
        Token t = first;
        while (t != null && t != stop) {
            int g = groupOpenedBy(t, groups);
            if (g >= 0) {
                OrCondition group = groups.get(g);
                Token altEnd = group.getEnd(choice[g]);
                for (Token u = group.getStart(choice[g]); u != null; u = u.next) {
                    append(buf, u);
                    if (u == altEnd) {
                        break;
                    }
                }
                // skip the rest of the group and its closing parenthesis
                t = group.getEnd(group.size() - 1).next;
                if (t != null) {
                    t = t.next;
                }
                continue;
            }
            append(buf, t);
            t = t.next;
        }
        return buf.append(')').toString();
    }

    /** The index of the group whose opening parenthesis this token is, or -1. */
    private static int groupOpenedBy(Token t, List<OrCondition> groups) {
        for (int g = 0; g < groups.size(); g++) {
            if (t.next == groups.get(g).getOrToken()) {
                return g;
            }
        }
        return -1;
    }

    private static void append(StringBuilder buf, Token t) {
        char last = buf.charAt(buf.length() - 1);
        if (last != '(' && !")".equals(t.image)) {
            buf.append(' ');
        }
        buf.append(t.image);
    }
}
