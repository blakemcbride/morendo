package org.jamocha.rete.util;

import org.jamocha.rete.BaseNode;

import java.util.Comparator;

public class NodeComparator implements Comparator<Object> {

    public NodeComparator() {}

    public int compare(Object left, Object right) {
        if (left instanceof BaseNode lnode && right instanceof BaseNode) {
            BaseNode rnode = (BaseNode) right;
            if (lnode.getNodeId() > rnode.getNodeId()) {
                return 1;
            }
            return -1;
        } else {
            return 0;
        }
    }
}
