/*
 * Copyright 2002-2009 Jamocha
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

import org.morendo.rete.BaseSlot;
import org.morendo.rete.Function;
import org.morendo.rete.Messages;
import org.morendo.rete.Rete;
import org.morendo.rete.Template;
import org.morendo.rule.util.GenerateFacts;

/**
 * Template validation will check the templates of the rule and make sure they are valid. If it
 * isn't, validate(Rule) will return false and provide details.
 *
 * @author Peter Lin
 */
public class TemplateValidation implements Analysis {

    /** */
    private Rete engine = null;

    private ErrorSummary error = null;
    private WarningSummary warning = null;

    public static final String INVALID_SLOT =
            Messages.getString("CompilerProperties.invalid.slot"); // $NON-NLS-1$
    public static final String INVALID_TEMPLATE =
            Messages.getString("CompilerProperties.invalid.template"); // $NON-NLS-1$
    public static final String INVALID_FUNCTION =
            Messages.getString("CompilerProperties.invalid.function"); // $NON-NLS-1$
    public static final String NO_FUNCTION =
            Messages.getString("CompilerProperties.no.function"); // $NON-NLS-1$
    public static final String NO_MODULE =
            Messages.getString("CompilerProperties.no.module"); // $NON-NLS-1$

    /** */
    public TemplateValidation(Rete engine) {
        this.engine = engine;
    }

    public Summary getErrors() {
        return this.error;
    }

    public void reset() {
        this.error = null;
        this.warning = null;
    }

    public Summary getWarnings() {
        return this.warning;
    }

    /** The conditions, followed by the members of every (not (and ...)) group among them. */
    private static Condition[] withGroupMembers(Condition[] cnds) {
        java.util.List<Condition> all = new java.util.ArrayList<>();
        for (Condition cnd : cnds) {
            all.add(cnd);
            if (cnd instanceof NotAndCondition group) {
                all.addAll(java.util.List.of(withGroupMembers(group.getConditions())));
            }
        }
        return all.toArray(new Condition[0]);
    }

    public int analyze(Rule rule) {
        int result = Analysis.VALIDATION_PASSED;
        this.error = new ErrorSummary();
        this.warning = new WarningSummary();
        this.checkForModule(rule);
        Condition[] cnds = withGroupMembers(rule.getConditions());
        for (int idx = 0; idx < cnds.length; idx++) {
            Condition cnd = cnds[idx];
            if (cnd instanceof ObjectCondition oc) {
                Template dft = oc.getTemplate();
                if (dft != null) {
                    Constraint[] cntrs = oc.getConstraints();
                    for (int idy = 0; idy < cntrs.length; idy++) {
                        Constraint cons = cntrs[idy];
                        if (cons instanceof LiteralConstraint) {
                            BaseSlot sl = dft.getSlot(cons.getName());
                            if (sl == null) {
                                this.error.addMessage(
                                        INVALID_SLOT
                                                + " "
                                                + cons.getName()
                                                + " slot does not exist.");
                                result = Analysis.VALIDATION_FAILED;
                            }
                        } else if (cons instanceof BoundConstraint bc) {
                            if (!bc.isObjectBinding) {
                                BaseSlot sl = dft.getSlot(bc.getName());
                                if (sl == null) {
                                    this.error.addMessage(
                                            INVALID_SLOT
                                                    + " "
                                                    + cons.getName()
                                                    + " slot does not exist.");
                                    result = Analysis.VALIDATION_FAILED;
                                }
                            }
                        } else if (cons instanceof PredicateConstraint pc) {
                            Function f = engine.findFunction(pc.getFunctionName());
                            if (f == null) {
                                addInvalidFunctionError(
                                        rule.getName() + "::" + pc.getFunctionName());
                            }
                        }
                    }
                } else {
                    this.error.addMessage(
                            INVALID_TEMPLATE
                                    + " "
                                    + oc.getTemplateName()
                                    + " template does not exist.");
                    result = Analysis.VALIDATION_FAILED;
                }
            } else if (cnd instanceof TestCondition tc) {
                if (tc.getFunction() == null) {
                    this.error.addMessage(NO_FUNCTION);
                    result = Analysis.VALIDATION_FAILED;
                } else {
                    Function f = tc.getFunction();
                    if (engine.findFunction(f.getName()) == null) {
                        addInvalidFunctionError(f.getName());
                        result = Analysis.VALIDATION_FAILED;
                    }
                }
            } else if (cnd instanceof ExistCondition) {

            }
        }
        // now we check the Right-hand side
        Action[] acts = rule.getActions();
        for (int idx = 0; idx < acts.length; idx++) {
            Action act = acts[idx];
            if (act instanceof FunctionAction fa) {
                if (engine.findFunction(fa.getFunctionName()) == null) {
                    addInvalidFunctionError(fa.getFunctionName());
                    result = Analysis.VALIDATION_FAILED;
                }
            }
        }
        return result;
    }

    protected void checkForModule(Rule rule) {
        if (rule.getName().indexOf("::") > 0) {
            String modname = GenerateFacts.parseModuleName(rule, this.engine);
            if (engine.findModule(modname) == null) {
                // add an error
                this.error.addMessage(NO_MODULE);
            }
        }
    }

    public void addInvalidFunctionError(String name) {
        this.error.addMessage(INVALID_FUNCTION + " " + name + " does not exist.");
    }
}
