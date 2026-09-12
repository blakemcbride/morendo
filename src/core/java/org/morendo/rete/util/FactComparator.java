package org.morendo.rete.util;

import org.morendo.rete.Fact;

import java.util.Comparator;

public class FactComparator implements Comparator<Object> {

    public FactComparator() {
        super();
    }

    public int compare(Object left, Object right) {
        Fact lf = (Fact) left;
        Fact rf = (Fact) right;
        if (lf.getFactId() > rf.getFactId()) {
            return 1;
        } else if (lf.getFactId() == rf.getFactId()) {
            return 0;
        } else {
            return -1;
        }
    }
}
