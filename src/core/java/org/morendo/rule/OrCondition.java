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
package org.morendo.rule;

import org.morendo.parser.clips.Token;
import org.morendo.rete.BaseNode;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.compiler.ConditionCompiler;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code (or CE+)} in a rule's left-hand side. It is never compiled: the parser expands a rule that
 * contains one into a rule per combination of alternatives, re-reading the rule's text with one
 * alternative in place of the group, which is why the group keeps the tokens of each alternative.
 */
public final class OrCondition implements Condition {

    private final List<Condition> alternatives = new ArrayList<>();
    private final List<Token> starts = new ArrayList<>();
    private final List<Token> ends = new ArrayList<>();
    private Token orToken = null;

    public OrCondition() {
        super();
    }

    /** Records an alternative with the tokens of its opening and closing parentheses. */
    public void addAlternative(Condition ce, Token start, Token end) {
        this.alternatives.add(ce);
        this.starts.add(start);
        this.ends.add(end);
    }

    public List<Condition> getAlternatives() {
        return this.alternatives;
    }

    public int size() {
        return this.alternatives.size();
    }

    public Token getStart(int alternative) {
        return this.starts.get(alternative);
    }

    public Token getEnd(int alternative) {
        return this.ends.get(alternative);
    }

    /** The {@code or} keyword token; the group's opening parenthesis is the token before it. */
    public Token getOrToken() {
        return this.orToken;
    }

    public void setOrToken(Token token) {
        this.orToken = token;
    }

    public boolean compare(Condition cond) {
        return cond instanceof OrCondition other && other.size() == size();
    }

    public List<?> getNodes() {
        return new ArrayList<>();
    }

    public void addNode(BaseNode node) {}

    public void addNewAlphaNodes(BaseNode node) {}

    public BaseNode getLastNode() {
        return null;
    }

    public void clear() {}

    public String toPPString() {
        StringBuilder buf = new StringBuilder("(or");
        for (Condition ce : this.alternatives) {
            buf.append(' ').append(ce.toPPString().trim());
        }
        return buf.append(')').toString();
    }

    public ConditionCompiler getCompiler(RuleCompiler ruleCompiler) {
        throw new IllegalStateException("an or group is expanded by the parser, not compiled");
    }

    public ConditionCompiler getCompiler(QueryCompiler ruleCompiler) {
        throw new IllegalStateException("an or group is expanded by the parser, not compiled");
    }

    public ConditionCompiler getCompiler(GraphQueryCompiler ruleCompiler) {
        throw new IllegalStateException("an or group is expanded by the parser, not compiled");
    }

    public List<Object> getBindConstraints() {
        return new ArrayList<>();
    }
}
