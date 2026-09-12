package org.jamocha.rete.util;

import org.jamocha.rete.Fact;

import java.util.Comparator;

public class FactTemplateComparator implements Comparator<Object> {

    public FactTemplateComparator() {
        super();
    }

    public int compare(Object left, Object right) {
        Fact lf = (Fact) left;
        Fact rf = (Fact) right;
        return lf.getDeftemplate().getName().compareTo(rf.getDeftemplate().getName());
    }
}
