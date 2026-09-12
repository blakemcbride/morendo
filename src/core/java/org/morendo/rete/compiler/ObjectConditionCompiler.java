package org.morendo.rete.compiler;

import org.morendo.rete.BaseAlpha;
import org.morendo.rete.BaseAlpha2;
import org.morendo.rete.BaseJoin;
import org.morendo.rete.BaseNode;
import org.morendo.rete.Binding;
import org.morendo.rete.CompileEvent;
import org.morendo.rete.DefaultQueryCompiler;
import org.morendo.rete.DefaultRuleCompiler;
import org.morendo.rete.GraphQueryCompiler;
import org.morendo.rete.HashedEqBNode;
import org.morendo.rete.HashedEqNJoin;
import org.morendo.rete.HashedNotEqBNode;
import org.morendo.rete.HashedNotEqNJoin;
import org.morendo.rete.LIANode;
import org.morendo.rete.NotJoin;
import org.morendo.rete.NotJoinFrst;
import org.morendo.rete.ObjectTypeNode;
import org.morendo.rete.PredicateBNode;
import org.morendo.rete.QueryCompiler;
import org.morendo.rete.RuleCompiler;
import org.morendo.rete.Template;
import org.morendo.rete.ZJBetaNode;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.query.QueryBaseAlpha;
import org.morendo.rete.query.QueryBaseAlphaCondition;
import org.morendo.rete.query.QueryBaseJoin;
import org.morendo.rete.query.QueryFuncJoin;
import org.morendo.rete.query.QueryHashedEqJoin;
import org.morendo.rete.query.QueryHashedEqNot;
import org.morendo.rete.query.QueryHashedNeqJoin;
import org.morendo.rete.query.QueryHashedNeqNot;
import org.morendo.rete.query.QueryLIANode;
import org.morendo.rete.query.QueryNotJoin;
import org.morendo.rete.query.QueryNotJoinFrst;
import org.morendo.rete.query.QueryObjTypeNode;
import org.morendo.rete.query.QueryParameterNode;
import org.morendo.rete.query.QueryZeroJoin;
import org.morendo.rule.AndLiteralConstraint;
import org.morendo.rule.BoundConstraint;
import org.morendo.rule.Condition;
import org.morendo.rule.Constraint;
import org.morendo.rule.Defquery;
import org.morendo.rule.GraphQuery;
import org.morendo.rule.LiteralConstraint;
import org.morendo.rule.ObjectCondition;
import org.morendo.rule.OrLiteralConstraint;
import org.morendo.rule.PredicateConstraint;
import org.morendo.rule.Query;
import org.morendo.rule.Rule;

/**
 * @author HouZhanbin Oct 12, 2007 9:42:15 AM
 */
public class ObjectConditionCompiler extends AbstractConditionCompiler {

    public ObjectConditionCompiler(RuleCompiler ruleCompiler) {
        this.ruleCompiler = (DefaultRuleCompiler) ruleCompiler;
    }

    public ObjectConditionCompiler(QueryCompiler queryCompiler) {
        this.queryCompiler = (DefaultQueryCompiler) queryCompiler;
    }

    public ObjectConditionCompiler(GraphQueryCompiler queryCompiler) {
        this.graphCompiler = queryCompiler;
    }

    /** Compile a single ObjectCondition and create the alphaNodes and/or Bindings */
    public void compile(Condition condition, int position, Rule util, boolean alphaMemory) {
        ObjectCondition cond = (ObjectCondition) condition;
        ObjectTypeNode otn = ruleCompiler.findObjectTypeNode(cond.getTemplateName());
        if (otn != null) {
            expandCrossPatternBindings(cond, util, position);
            BaseAlpha first = null;
            BaseAlpha previous = null;
            BaseAlpha current = null;
            Template templ = cond.getTemplate();

            Constraint[] constrs = cond.getConstraints();
            for (int idx = 0; idx < constrs.length; idx++) {
                Constraint cnstr = constrs[idx];
                if (cnstr instanceof LiteralConstraint literalConstraint) {
                    current = ruleCompiler.compileConstraint(literalConstraint, templ, util);
                } else if (cnstr instanceof AndLiteralConstraint andLiteralConstraint) {
                    current = ruleCompiler.compileConstraint(andLiteralConstraint, templ, util);
                } else if (cnstr instanceof OrLiteralConstraint orLiteralConstraint) {
                    current = ruleCompiler.compileConstraint(orLiteralConstraint, templ, util);
                } else if (cnstr instanceof BoundConstraint bc) {
                    BaseAlpha2 ifn = ruleCompiler.compileConstraint(bc, templ, util, position);
                    if (ifn != null) {
                        current = ifn;
                    }
                } else if (cnstr instanceof PredicateConstraint pcon) {
                    current = ruleCompiler.compileConstraint(pcon, templ, util, position);
                    if (pcon.isPredicateJoin()) {
                        cond.setHasPredicateJoin(true);
                    }
                }
                // we add the node to the previous
                if (first == null) {
                    if (current != null) {
                        first = current;
                        previous = current;
                    }
                } else if (current != null && current != previous) {
                    try {
                        previous.addSuccessorNode(
                                current, ruleCompiler.getEngine(), ruleCompiler.getMemory());
                        // now set the previous to current
                        previous = current;
                    } catch (AssertException e) {
                        // send an event
                    }
                }
            }
            if (first != null) {
                attachAlphaNode(otn, first, cond);
            }
        }
    }

    public void compile(Condition condition, int position, Query query) {
        if (query instanceof GraphQuery graphQuery) {
            compile(condition, position, graphQuery);
        } else {
            ObjectCondition cond = (ObjectCondition) condition;
            QueryObjTypeNode queryOTN = queryCompiler.findQueryObjTypeNode(cond.getTemplate());

            if (queryOTN != null) {
                QueryBaseAlpha first = null;
                QueryBaseAlpha previous = null;
                QueryBaseAlpha current = null;
                Template templ = cond.getTemplate();

                Constraint[] constrs = cond.getConstraints();
                for (int idx = 0; idx < constrs.length; idx++) {
                    Constraint cnstr = constrs[idx];
                    if (cnstr instanceof LiteralConstraint literalConstraintValue) {
                        current =
                                queryCompiler.compileConstraint(
                                        literalConstraintValue, templ, query);
                    } else if (cnstr instanceof AndLiteralConstraint andLiteralConstraintValue) {
                        current =
                                queryCompiler.compileConstraint(
                                        andLiteralConstraintValue, templ, query);
                    } else if (cnstr instanceof OrLiteralConstraint orLiteralConstraintValue) {
                        current =
                                queryCompiler.compileConstraint(
                                        orLiteralConstraintValue, templ, query);
                    } else if (cnstr instanceof BoundConstraint boundConstraint) {
                        current =
                                queryCompiler.compileConstraint(
                                        boundConstraint, templ, query, position);
                    } else if (cnstr instanceof PredicateConstraint pcon) {
                        current = queryCompiler.compileConstraint(pcon, templ, query, position);
                    }
                    // we add the node to the previous
                    if (first == null) {
                        if (current != null) {
                            first = current;
                            previous = current;
                        }
                    } else if (current != null && current != previous) {
                        try {
                            previous.addSuccessorNode(current, queryCompiler.getEngine(), null);
                            // now set the previous to current
                            previous = current;
                        } catch (AssertException e) {
                            // send an event
                        }
                    }
                }
                if (first != null) {
                    try {
                        // Note: This differs from rules, since we do not share nodes for queries.
                        queryOTN.addSuccessorNode(first, queryCompiler.getEngine(), null);
                        cond.addNewAlphaNodes(first);
                    } catch (AssertException e) {
                    }
                }
            }
        }
    }

    public void compile(Condition condition, int position, GraphQuery query) {
        ObjectCondition cond = (ObjectCondition) condition;
        QueryObjTypeNode queryOTN = graphCompiler.findQueryObjTypeNode(cond.getTemplate());
        if (queryOTN != null) {
            QueryBaseAlpha first = null;
            QueryBaseAlpha previous = null;
            QueryBaseAlpha current = null;
            Template templ = cond.getTemplate();

            Constraint[] constrs = cond.getConstraints();
            for (int idx = 0; idx < constrs.length; idx++) {
                Constraint cnstr = constrs[idx];
                if (cnstr instanceof LiteralConstraint) {
                    current =
                            graphCompiler.compileConstraint(
                                    (LiteralConstraint) cnstr, templ, query);
                } else if (cnstr instanceof AndLiteralConstraint) {
                    current =
                            graphCompiler.compileConstraint(
                                    (AndLiteralConstraint) cnstr, templ, query);
                } else if (cnstr instanceof OrLiteralConstraint) {
                    current =
                            graphCompiler.compileConstraint(
                                    (OrLiteralConstraint) cnstr, templ, query);
                } else if (cnstr instanceof BoundConstraint boundConstraintValue) {
                    current =
                            graphCompiler.compileConstraint(
                                    boundConstraintValue, templ, query, position);
                } else if (cnstr instanceof PredicateConstraint pcon) {
                    current = graphCompiler.compileConstraint(pcon, templ, query, position);
                }
                // we add the node to the previous
                if (first == null) {
                    if (current != null) {
                        first = current;
                        previous = current;
                    }
                } else if (current != null && current != previous) {
                    try {
                        if (current instanceof QueryParameterNode queryParameterNode) {
                            String pname = (queryParameterNode).getParameterName();
                            if (query.isQueryParameter(pname)) {
                                previous.addSuccessorNode(current, graphCompiler.getEngine(), null);
                                previous = current;
                            }
                        } else {
                            previous.addSuccessorNode(current, graphCompiler.getEngine(), null);
                            previous = current;
                        }
                    } catch (AssertException e) {
                        // send an event
                    }
                }
            }
            if (first != null && !(first instanceof QueryParameterNode)) {
                try {
                    // Note: This differs from rules, since we do not share nodes for queries.
                    queryOTN.addSuccessorNode(first, graphCompiler.getEngine(), null);
                    cond.addNewAlphaNodes(first);
                } catch (AssertException e) {
                }
            }
        }
    }

    /**
     * A chained binding such as (slot ?x&~?y) is an intra-fact comparison only when ?y is a slot of
     * the same pattern. When ?y was bound by an earlier pattern it is a join with that pattern, so
     * it becomes an ordinary (negated) bound constraint on the slot.
     */
    private void expandCrossPatternBindings(ObjectCondition cond, Rule rule, int position) {
        for (Constraint c : cond.getConstraints()) {
            if (c instanceof BoundConstraint bc && bc.hasIntraFactJoin()) {
                java.util.List<BoundConstraint> sameFact = new java.util.ArrayList<>();
                for (BoundConstraint other : bc.getIntraFactJoins()) {
                    org.morendo.rete.Binding bound = rule.getBinding(other.getVariableName());
                    if (bound != null && bound.getLeftRow() != position) {
                        BoundConstraint join = new BoundConstraint();
                        join.setName(bc.getName());
                        join.setValue(other.getVariableName());
                        join.setNegated(other.getNegated());
                        cond.addConstraint(join);
                    } else {
                        sameFact.add(other);
                    }
                }
                bc.setIntraFactJoins(sameFact);
            }
        }
    }

    /**
     * For now just attach the node and don't bother with node sharing
     *
     * @param existing - an existing node in the network. it may be an ObjectTypeNode or AlphaNode
     * @param alpha
     */
    public void attachAlphaNode(BaseAlpha existing, BaseAlpha alpha, Condition cond) {
        if (alpha != null) {
            try {
                BaseAlpha share = null;
                share = shareAlphaNode(existing, alpha);
                if (share == null) {
                    existing.addSuccessorNode(
                            alpha, ruleCompiler.getEngine(), ruleCompiler.getMemory());
                    // if the node isn't shared, we add the node to the Condition
                    // object the node belongs to.
                    cond.addNewAlphaNodes(alpha);
                } else if (existing != alpha) {
                    // the node is shared, so instead of adding the new node,
                    // we add the existing node
                    share.incrementUseCount();
                    cond.addNode(share);
                    ruleCompiler.getMemory().removeAlphaMemory(alpha);
                    if (alpha.successorCount() == 1
                            && alpha.getSuccessorNodes()[0] instanceof BaseAlpha) {
                        // get the next node from the new AlphaNode
                        BaseAlpha nnext = (BaseAlpha) alpha.getSuccessorNodes()[0];
                        attachAlphaNode(share, nnext, cond);
                    }
                }
            } catch (AssertException e) {
                // send an event with the correct error
                CompileEvent ce = new CompileEvent(this, CompileEvent.Kind.ADD_NODE_ERROR);
                ce.setMessage(alpha.toPPString());
                ruleCompiler.notifyListener(ce);
            }
        }
    }

    /**
     * Implementation will get the hashString from each node and compare them
     *
     * @param otn
     * @param alpha
     * @return
     */
    private BaseAlpha shareAlphaNode(BaseAlpha existing, BaseAlpha alpha) {
        Object[] scc = existing.getSuccessorNodes();
        for (int idx = 0; idx < scc.length; idx++) {
            Object next = scc[idx];
            if (next instanceof BaseAlpha baseAlpha) {
                if (baseAlpha.hashString().equals(alpha.hashString())) {
                    return baseAlpha;
                }
            }
        }
        return null;
    }

    public void compileFirstJoin(Condition condition, Rule rule) throws AssertException {
        ObjectCondition cond = (ObjectCondition) condition;
        ObjectTypeNode otn = ruleCompiler.findObjectTypeNode(cond.getTemplateName());
        // the LeftInputAdapterNode is the first node to propogate to
        // the first joinNode of the rule
        LIANode node = new LIANode(ruleCompiler.getEngine().nextNodeId());
        // if the condition doesn't have any nodes, we want to add it to
        // the objectType node if one doesn't already exist.
        // otherwise we add it to the last AlphaNode
        if (cond.getNodes().size() == 0) {
            // try to find the existing LIANode for the given ObjectTypeNode
            // if we don't do this, we end up with multiple LIANodes
            // descending directly from the ObjectTypeNode
            LIANode existingLIANode = ruleCompiler.findLIANode(otn);
            if (existingLIANode == null) {
                otn.addSuccessorNode(node, ruleCompiler.getEngine(), ruleCompiler.getMemory());
                cond.addNode(node);
            } else {
                existingLIANode.incrementUseCount();
                cond.addNode(existingLIANode);
            }
        } else {
            // add the LeftInputAdapterNode to the last alphaNode
            // In the case of node sharing, the LIANode could be the last
            // alphaNode, so we have to check and only add the node to
            // the condition if it isn't a LIANode
            BaseAlpha old = (BaseAlpha) cond.getLastNode();
            // if the last node of condition has a LIANode successor,
            // the LIANode should be shared with the new CE followed by another CE.
            // Houzhanbin,10/16/2007
            BaseNode[] successors = (BaseNode[]) old.getSuccessorNodes();
            for (int i = 0; i < successors.length; i++) {
                if (successors[i] instanceof LIANode) {
                    cond.addNode(successors[i]);
                    return;
                }
            }

            if (!(old instanceof LIANode)) {
                old.addSuccessorNode(node, ruleCompiler.getEngine(), ruleCompiler.getMemory());
                cond.addNode(node);
            }
        }
    }

    public void compileFirstJoin(Condition condition, Query query) throws AssertException {
        if (query instanceof GraphQuery graphQueryValue) {
            compileFirstJoin(condition, graphQueryValue);
        } else {
            Defquery dquery = (Defquery) query;
            ObjectCondition cond = (ObjectCondition) condition;
            Template template = this.queryCompiler.getEngine().findTemplate(cond.getTemplateName());
            QueryObjTypeNode queryotn = dquery.getQueryRootNode().findQueryObjTypeNode(template);

            QueryLIANode node = new QueryLIANode(queryCompiler.getEngine().nextNodeId());
            // if the condition doesn't have any nodes, we want to add it to
            // the objectType node if one doesn't already exist.
            // otherwise we add it to the last AlphaNode
            if (cond.getNodes().size() == 0) {
                // try to find the existing LIANode for the given ObjectTypeNode
                // if we don't do this, we end up with multiple LIANodes
                // descending directly from the ObjectTypeNode
                QueryLIANode existingLIANode = queryCompiler.findQueryLIANode(queryotn);
                if (existingLIANode == null) {
                    queryotn.addSuccessorNode(node, queryCompiler.getEngine(), null);
                    cond.addNode(node);
                } else {
                    existingLIANode.incrementUseCount();
                    cond.addNode(existingLIANode);
                }
            } else {
                // add the LeftInputAdapterNode to the last alphaNode
                // In the case of node sharing, the LIANode could be the last
                // alphaNode, so we have to check and only add the node to
                // the condition if it isn't a LIANode
                QueryBaseAlphaCondition old = (QueryBaseAlphaCondition) cond.getLastNode();
                // if the last node of condition has a LIANode successor,
                // the LIANode should be shared with the new CE followed by another CE.
                // Houzhanbin,10/16/2007
                BaseNode[] successors = (BaseNode[]) old.getSuccessorNodes();
                for (int i = 0; i < successors.length; i++) {
                    if (successors[i] instanceof LIANode) {
                        cond.addNode(successors[i]);
                        return;
                    }
                }
                old.addSuccessorNode(node, queryCompiler.getEngine(), null);
                cond.addNode(node);
            }
        }
    }

    public void compileFirstJoin(Condition condition, GraphQuery query) throws AssertException {
        ObjectCondition cond = (ObjectCondition) condition;
        Template template = this.graphCompiler.getEngine().findTemplate(cond.getTemplateName());
        QueryObjTypeNode queryotn = this.graphCompiler.findQueryObjTypeNode(template);

        QueryLIANode node = new QueryLIANode(graphCompiler.getEngine().nextNodeId());
        if (cond.getNodes().size() == 0) {

            QueryLIANode existingLIANode = graphCompiler.findQueryLIANode(queryotn);
            if (existingLIANode == null) {
                queryotn.addSuccessorNode(node, graphCompiler.getEngine(), null);
                cond.addNode(node);
            } else {
                existingLIANode.incrementUseCount();
                cond.addNode(existingLIANode);
            }
        } else {
            // add the LeftInputAdapterNode to the last alphaNode
            // In the case of node sharing, the LIANode could be the last
            // alphaNode, so we have to check and only add the node to
            // the condition if it isn't a LIANode
            QueryBaseAlphaCondition old = (QueryBaseAlphaCondition) cond.getLastNode();
            // if the last node of condition has a LIANode successor,
            // the LIANode should be shared with the new CE followed by another CE.
            // Houzhanbin,10/16/2007
            BaseNode[] successors = (BaseNode[]) old.getSuccessorNodes();
            for (int i = 0; i < successors.length; i++) {
                if (successors[i] instanceof LIANode) {
                    cond.addNode(successors[i]);
                    return;
                }
            }
            old.addSuccessorNode(node, graphCompiler.getEngine(), null);
            cond.addNode(node);
        }
    }

    /** method compiles ObjectConditions, which include NOTCE */
    public BaseJoin compileJoin(
            Condition condition, int position, Rule rule, Condition previousCond) {

        Binding[] binds = getBindings(condition, rule, position);
        ObjectCondition oc = (ObjectCondition) condition;
        BaseJoin joinNode = null;
        // deal with the CE which is not NOT CE.
        if (!oc.getNegated()) {
            if (binds.length > 0 && oc.isHasPredicateJoin()) {
                joinNode = new PredicateBNode(ruleCompiler.getEngine().nextNodeId());
            } else if (binds.length > 0 && oc.isHasNotEqual()) {
                joinNode = new HashedNotEqBNode(ruleCompiler.getEngine().nextNodeId());
            } else if (binds.length > 0) {
                joinNode = new HashedEqBNode(ruleCompiler.getEngine().nextNodeId());
            } else if (binds.length == 0) {
                joinNode = new ZJBetaNode(ruleCompiler.getEngine().nextNodeId());
            }
        }

        // deal with the CE which is NOT CE.
        if (oc.getNegated()) {
            if (binds.length > 0 && oc.isHasPredicateJoin()) {
                joinNode = new NotJoin(ruleCompiler.getEngine().nextNodeId());
            } else if (oc.isHasNotEqual()) {
                joinNode = new HashedNotEqNJoin(ruleCompiler.getEngine().nextNodeId());
            } else {
                joinNode = new HashedEqNJoin(ruleCompiler.getEngine().nextNodeId());
            }
        }

        joinNode.setBindings(binds);
        return joinNode;
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, Query query, Condition previousCond) {
        if (query instanceof GraphQuery) {
            return compileJoin(condition, position, (GraphQuery) query, previousCond);
        } else {
            Binding[] binds = getBindings(condition, query, position);
            ObjectCondition oc = (ObjectCondition) condition;
            QueryBaseJoin joinNode = null;
            // deal with the CE which is not NOT CE.
            if (!oc.getNegated()) {
                if (binds.length > 0 && oc.isHasPredicateJoin()) {
                    joinNode = new QueryFuncJoin(queryCompiler.getEngine().nextNodeId());
                } else if (binds.length > 0 && oc.isHasNotEqual()) {
                    joinNode = new QueryHashedNeqJoin(queryCompiler.getEngine().nextNodeId());
                } else if (binds.length > 0) {
                    joinNode = new QueryHashedEqJoin(queryCompiler.getEngine().nextNodeId());
                } else if (binds.length == 0) {
                    joinNode = new QueryZeroJoin(queryCompiler.getEngine().nextNodeId());
                }
            }

            // deal with the CE which is NOT CE.
            if (oc.getNegated()) {
                if (binds.length > 0 && oc.isHasPredicateJoin()) {
                    joinNode = new QueryNotJoin(queryCompiler.getEngine().nextNodeId());
                } else if (oc.isHasNotEqual()) {
                    joinNode = new QueryHashedNeqNot(queryCompiler.getEngine().nextNodeId());
                } else {
                    joinNode = new QueryHashedEqNot(queryCompiler.getEngine().nextNodeId());
                }
            }

            joinNode.setBindings(binds);
            return joinNode;
        }
    }

    public QueryBaseJoin compileJoin(
            Condition condition, int position, GraphQuery query, Condition previousCond) {
        Binding[] binds = getBindings(condition, query, position);
        ObjectCondition oc = (ObjectCondition) condition;
        QueryBaseJoin joinNode = null;
        // deal with the CE which is not NOT CE.
        if (!oc.getNegated()) {
            if (binds.length > 0 && oc.isHasPredicateJoin()) {
                joinNode = new QueryFuncJoin(graphCompiler.getEngine().nextNodeId());
            } else if (binds.length > 0 && oc.isHasNotEqual()) {
                joinNode = new QueryHashedNeqJoin(graphCompiler.getEngine().nextNodeId());
            } else if (binds.length > 0) {
                joinNode = new QueryHashedEqJoin(graphCompiler.getEngine().nextNodeId());
            } else if (binds.length == 0) {
                joinNode = new QueryZeroJoin(graphCompiler.getEngine().nextNodeId());
            }
        }

        // deal with the CE which is NOT CE.
        if (oc.getNegated()) {
            if (binds.length > 0 && oc.isHasPredicateJoin()) {
                joinNode = new QueryNotJoin(graphCompiler.getEngine().nextNodeId());
            } else if (oc.isHasNotEqual()) {
                joinNode = new QueryHashedNeqNot(graphCompiler.getEngine().nextNodeId());
            } else {
                joinNode = new QueryHashedEqNot(graphCompiler.getEngine().nextNodeId());
            }
        }

        joinNode.setBindings(binds);
        return joinNode;
    }

    @Override
    ObjectCondition getObjectCondition(Condition condition) {
        return (ObjectCondition) condition;
    }

    public void compileSingleCE(Rule rule) throws AssertException {
        Condition[] conds = rule.getConditions();
        ObjectCondition oc = (ObjectCondition) conds[0];
        if (oc.getNegated()) {
            // the ObjectCondition is negated, so we need to
            // handle it appropriate. This means we need to
            // add a LIANode to _IntialFact and attach a NOTNode
            // to the LIANode.
            ObjectTypeNode otn =
                    this.ruleCompiler.getInputnodes().get(ruleCompiler.getEngine().getInitFact());
            LIANode lianode = ruleCompiler.findLIANode(otn);
            NotJoinFrst njoin = new NotJoinFrst(ruleCompiler.getEngine().nextNodeId());
            njoin.setBindings(new Binding[0]);
            ruleCompiler.attachJoinNode(lianode, njoin);
            // add the join to the rule object
            rule.addJoinNode(njoin);
            ruleCompiler.attachJoinNode(oc.getLastNode(), njoin);
        } else if (oc.getNodes().size() == 0) {
            // this means the rule has a binding, but no conditions
            ObjectTypeNode otn = ruleCompiler.findObjectTypeNode(oc.getTemplateName());
            LIANode lianode = new LIANode(ruleCompiler.getEngine().nextNodeId());
            otn.addSuccessorNode(lianode, ruleCompiler.getEngine(), ruleCompiler.getMemory());
            rule.getConditions()[0].addNode(lianode);
        }
    }

    public void compileSingleCE(Query query) throws AssertException {
        Condition[] conds = query.getConditions();
        ObjectCondition oc = (ObjectCondition) conds[0];
        Defquery dquery = (Defquery) query;
        if (oc.getNegated()) {
            Template template =
                    this.queryCompiler
                            .getEngine()
                            .findTemplate(queryCompiler.getEngine().getInitFact().getName());
            QueryObjTypeNode queryotn = dquery.getQueryRootNode().findQueryObjTypeNode(template);
            QueryLIANode querylianode = queryCompiler.findQueryLIANode(queryotn);

            QueryNotJoinFrst njoin = new QueryNotJoinFrst(queryCompiler.getEngine().nextNodeId());
            njoin.setBindings(new Binding[0]);
            querylianode.addSuccessorNode(njoin, queryCompiler.getEngine(), null);
            // add the join to the rule object
            query.addJoinNode(njoin);
            oc.getLastNode().addSuccessorNode(njoin, queryCompiler.getEngine(), null);
        } else if (oc.getNodes().size() == 0) {
            Template template =
                    this.queryCompiler
                            .getEngine()
                            .findTemplate(queryCompiler.getEngine().getInitFact().getName());
            QueryObjTypeNode queryotn = dquery.getQueryRootNode().findQueryObjTypeNode(template);
            // this means the rule has a binding, but no conditions
            QueryLIANode lianode = new QueryLIANode(queryCompiler.getEngine().nextNodeId());
            queryotn.addSuccessorNode(lianode, queryCompiler.getEngine(), null);
            query.getConditions()[0].addNode(lianode);
        } else {

        }
    }
}
