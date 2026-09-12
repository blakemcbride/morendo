/*
 * Copyright 2002-2008 Peter Lin
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
package org.morendo.rete.compiler;

import org.morendo.rete.DefaultQueryCompiler;
import org.morendo.rete.DefaultRuleCompiler;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.Rete;
import org.morendo.rete.RuleCompiler;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Holds the condition compilers for one engine.
 *
 * <p>This used to be a JVM-wide singleton initialised by whichever rule, query or graph-query
 * compiler asked for it first. Every further Rete instance in the same JVM then compiled its rules
 * against the first engine's network (or failed with a NullPointerException), and within one engine
 * only one of the three compiler references was ever set, so mixing defrule and defquery broke.
 * Providers are now created per engine, and every condition compiler is wired to that engine's
 * rule, query and graph-query compilers.
 */
public class CompilerProvider {

    private static final Map<Rete, CompilerProvider> providers = new WeakHashMap<>();

    public final ConditionCompiler objectConditionCompiler;
    public final ConditionCompiler existConditionCompiler;
    public final ConditionCompiler temporalConditionCompiler;
    public final ConditionCompiler testConditionCompiler;
    public final ConditionCompiler andConditionCompiler;
    public final ConditionCompiler cubeQueryConditionCompiler;
    public final ConditionCompiler onlyConditionCompiler;
    public final ConditionCompiler multipleConditionCompiler;
    public final ConditionCompiler forallConditionCompiler;

    private CompilerProvider(Rete engine) {
        DefaultRuleCompiler rc = (DefaultRuleCompiler) engine.getRuleCompiler();
        DefaultQueryCompiler qc = (DefaultQueryCompiler) engine.getQueryCompiler();
        GraphQueryCompiler gc = engine.getGraphQueryCompiler();
        objectConditionCompiler = wire(new ObjectConditionCompiler(rc), rc, qc, gc);
        existConditionCompiler =
                wire(new ExistConditionCompiler(objectConditionCompiler), rc, qc, gc);
        temporalConditionCompiler = wire(new TemporalConditionCompiler(rc), rc, qc, gc);
        testConditionCompiler = wire(new TestConditionCompiler(rc), rc, qc, gc);
        andConditionCompiler = wire(new AndConditionCompiler(), rc, qc, gc);
        cubeQueryConditionCompiler = wire(new CubeQueryConditionCompiler(rc), rc, qc, gc);
        onlyConditionCompiler =
                wire(new OnlyConditionCompiler(objectConditionCompiler), rc, qc, gc);
        multipleConditionCompiler =
                wire(new MultipleConditionCompiler(objectConditionCompiler), rc, qc, gc);
        forallConditionCompiler =
                wire(new ForallConditionCompiler(objectConditionCompiler), rc, qc, gc);
    }

    private static ConditionCompiler wire(
            ConditionCompiler compiler,
            DefaultRuleCompiler rc,
            DefaultQueryCompiler qc,
            GraphQueryCompiler gc) {
        if (compiler instanceof AbstractConditionCompiler acc) {
            acc.ruleCompiler = rc;
            acc.queryCompiler = qc;
            acc.graphCompiler = gc;
        } else if (compiler instanceof TestConditionCompiler tcc) {
            tcc.ruleCompiler = rc;
            tcc.queryCompiler = qc;
            tcc.graphCompiler = gc;
        } else if (compiler instanceof ForallConditionCompiler fcc) {
            fcc.ruleCompiler = rc;
            fcc.queryCompiler = qc;
            fcc.graphCompiler = gc;
        } else {
            throw new IllegalStateException("cannot wire " + compiler.getClass().getName());
        }
        return compiler;
    }

    public static synchronized CompilerProvider getInstance(Rete engine) {
        CompilerProvider provider = providers.get(engine);
        if (provider == null) {
            provider = new CompilerProvider(engine);
            providers.put(engine, provider);
        }
        return provider;
    }

    public static CompilerProvider getInstance(RuleCompiler ruleCompiler) {
        if (!(ruleCompiler instanceof DefaultRuleCompiler)) {
            throw new IllegalArgumentException(
                    "unsupported rule compiler " + ruleCompiler.getClass().getName());
        }
        return getInstance(((DefaultRuleCompiler) ruleCompiler).getEngine());
    }

    public static CompilerProvider getInstance(QueryCompiler queryCompiler) {
        if (!(queryCompiler instanceof DefaultQueryCompiler)) {
            throw new IllegalArgumentException(
                    "unsupported query compiler " + queryCompiler.getClass().getName());
        }
        return getInstance(((DefaultQueryCompiler) queryCompiler).getEngine());
    }

    public static CompilerProvider getInstance(GraphQueryCompiler queryCompiler) {
        return getInstance(queryCompiler.getEngine());
    }
}
