package org.morendo.rete;

import org.morendo.messagerouter.MessageRouter;
import org.morendo.rete.exception.AssertException;
import org.morendo.rete.exception.RetractException;

import java.util.List;

/**
 * Interface defining Rule engine, with all the methods common to interpreted and statically
 * compiled versions.
 *
 * @author Peter Lin
 */
public interface Engine {
    ActivationList getActivationList();

    Agenda getAgenda();

    Object getBinding(String name);

    Object getDefglobalValue(String name);

    MessageRouter getMessageRouter();

    int getRulesFiredCount();

    List<?> getRulesFired();

    Strategy getStrategy();

    WorkingMemory getWorkingMemory();

    long nextFactId();

    void assertObject(Object value) throws AssertException;

    void assertObjects(List<?> values) throws AssertException;

    void retractObject(Object value) throws RetractException;

    void retractObjects(List<?> values) throws RetractException;

    void declareObject(Class<?> obj);

    void resetAll();

    void writeMessage(String msg);
}
