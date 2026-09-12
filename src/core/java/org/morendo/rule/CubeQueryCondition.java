package org.morendo.rule;

import org.morendo.rete.Constants;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.compiler.CompilerProvider;
import org.morendo.rete.compiler.ConditionCompiler;

import java.util.ArrayList;
import java.util.List;

public final class CubeQueryCondition extends ObjectCondition {

    /** */
    public CubeQueryCondition() {
        super();
    }

    @SuppressWarnings("static-access")
    public ConditionCompiler getCompiler(RuleCompiler ruleCompiler) {
        return CompilerProvider.getInstance(ruleCompiler).cubeQueryConditionCompiler;
    }

    public List<Object> getQueryConstraints() {
        ArrayList<Object> binds = new ArrayList<>();
        for (Object c : constraints) {
            if (c instanceof BoundConstraint bc) {
                if (!bc.firstDeclaration() && !bc.getIsObjectBinding()) {
                    binds.add(c);
                }
            } else if (c instanceof PredicateConstraint pc) {
                binds.add(pc);
            }
        }
        return binds;
    }

    public String toPPString(int tabs) {
        StringBuilder buf = new StringBuilder();
        buf.append("(cubequery" + Constants.LINEBREAK);
        buf.append(")" + Constants.LINEBREAK);
        return buf.toString();
    }
}
