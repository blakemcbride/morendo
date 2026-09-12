package org.morendo.rete.functions.memory;

import org.morendo.rete.BaseAlpha;
import org.morendo.rete.BaseJoin;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.DefaultWM;
import org.morendo.rete.ExistJoin;
import org.morendo.rete.Fact;
import org.morendo.rete.Function;
import org.morendo.rete.HashedAlphaMemoryImpl;
import org.morendo.rete.HashedEqBNode;
import org.morendo.rete.HashedEqNJoin;
import org.morendo.rete.HashedNeqAlphaMemory;
import org.morendo.rete.HashedNotEqBNode;
import org.morendo.rete.HashedNotEqNJoin;
import org.morendo.rete.Index;
import org.morendo.rete.LIANode;
import org.morendo.rete.NotJoin;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;
import org.morendo.rete.functions.BaseMatchFunction;
import org.morendo.rule.Condition;
import org.morendo.rule.Defrule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuleMatchesFunction extends BaseMatchFunction implements Function {

    /** */
    public static final String RULE_MATCHES = "rule-matches";

    public RuleMatchesFunction() {}

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (params != null && params.length > 0) {
            ArrayList<Defrule> rules = new ArrayList<>();
            for (int idx = 0; idx < params.length; idx++) {
                if (params[idx] instanceof ValueParam) {
                    String name = params[idx].getStringValue();
                    Defrule r = (Defrule) engine.getCurrentFocus().findRule(name);
                    if (r != null && !rules.contains(r)) {
                        rules.add(r);
                    }
                }
            }
            DefaultWM wm = (DefaultWM) engine.getWorkingMemory();
            // iterate over the rules
            for (int idx = 0; idx < rules.size(); idx++) {
                this.printRuleMemories(engine, rules.get(idx), wm);
            }
        }
        return new DefaultReturnVector();
    }

    protected void printRuleMemories(Rete engine, Defrule rule, DefaultWM wm) {
        StringBuilder buf = new StringBuilder();
        buf.append(rule.getName() + Constants.LINEBREAK);
        Condition[] conditions = rule.getConditions();
        for (int idx = 0; idx < conditions.length; idx++) {
            Condition c = conditions[idx];
            List<?> nodes = c.getNodes();
            Iterator<?> itr = nodes.iterator();
            while (itr.hasNext()) {
                BaseAlpha n = (BaseAlpha) itr.next();
                if (!(n instanceof LIANode)) {
                    Map<?, ?> rmem = wm.getBetaRightMemory(n);
                    buf.append(
                            n.toPPString()
                                    + " - right memories:"
                                    + rmem.size()
                                    + Constants.LINEBREAK);
                    Iterator<?> memItr = rmem.keySet().iterator();
                    while (memItr.hasNext()) {
                        Fact f = (Fact) memItr.next();
                        buf.append("\t" + f.toFactString() + Constants.LINEBREAK);
                    }
                }
            }
        }
        List<?> betaNodes = rule.getJoins();
        Iterator<?> bnItr = betaNodes.iterator();
        while (bnItr.hasNext()) {
            BaseJoin betaNode = (BaseJoin) bnItr.next();
            buf.append(betaNode.toPPString() + Constants.LINEBREAK);
            Map<?, ?> lmem = wm.getBetaLeftMemory(betaNode);
            Object rmem = wm.getBetaRightMemory(betaNode);
            if (lmem.size() > 0) {
                buf.append(" - left memories:" + Constants.LINEBREAK);
                Iterator<?> leftItr = lmem.keySet().iterator();
                while (leftItr.hasNext()) {
                    Index mem = (Index) leftItr.next();
                    buf.append("\t" + mem.toPPString() + Constants.LINEBREAK);
                }
            }

            buf.append(" - right memories:" + Constants.LINEBREAK);
            // now iterate over the right memories
            if (betaNode instanceof HashedEqBNode || betaNode instanceof HashedEqNJoin) {
                HashedAlphaMemoryImpl haMem = (HashedAlphaMemoryImpl) rmem;
                Object[] facts = haMem.iterateAll();
                for (int idx = 0; idx < facts.length; idx++) {
                    Fact f = (Fact) facts[idx];
                    buf.append("\t" + f.toFactString() + Constants.LINEBREAK);
                }
            } else if (betaNode instanceof HashedNotEqNJoin
                    || betaNode instanceof HashedNotEqBNode) {
                HashedNeqAlphaMemory haneqMem = (HashedNeqAlphaMemory) rmem;
                Object[] facts = haneqMem.iterateAll();
                for (int idx = 0; idx < facts.length; idx++) {
                    Fact f = (Fact) facts[idx];
                    buf.append("\t" + f.toFactString() + Constants.LINEBREAK);
                }
            } else if (betaNode instanceof ExistJoin || betaNode instanceof NotJoin) {
                Map<?, ?> rmMem = (Map<?, ?>) rmem;
                Iterator<?> itr = rmMem.keySet().iterator();
                while (itr.hasNext()) {
                    Fact f = (Fact) itr.next();
                    buf.append("\t" + f.toFactString() + Constants.LINEBREAK);
                }
            }
            buf.append(Constants.LINEBREAK);
        }
        engine.writeMessage(buf.toString());
    }

    public String getName() {
        return RULE_MATCHES;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String[].class};
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(rule-matches <Rule name>)"
                + "Function description:\n"
                + "\tPrints out the memories for a rule.";
    }
}
