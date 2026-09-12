/*
 * Copyright 2002-2008 Peter Lin
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
package org.jamocha.rete;

import java.util.EventObject;

/**
 * @author Peter Lin
 *
 * EngineEvent is a generic event class. Rather than have a bunch of
 * event subclasses, the current design uses event type code.
 */
public class EngineEvent extends EventObject {
	private static final long serialVersionUID = 1L;

    /**
     * 
     */
	/** What an engine event reports. */
	public enum Kind { ASSERT, RETRACT, PROFILE, ASSERT_RETRACT, ASSERT_RETRACT_PROFILE, ASSERT_PROFILE }
 
    /**
     * the default value is assert event
     */
    private Kind typeCode = Kind.ASSERT;
    private transient BaseNode sourceNode = null;
    private Fact[] facts = null;

    /**
     * 
     * @param source - the source should be either the workingMemory or Rete
     * @param typeCode - event type
     * @param sourceNode - the node which initiated the event
     */
	public EngineEvent(Object source, Kind typeCode, BaseNode sourceNode, Fact[] facts) {
		super(source);
        this.typeCode = typeCode;
        this.sourceNode = sourceNode;
        this.facts = facts;
	}
    
    public Kind getEventType() {
        return this.typeCode;
    }
    
    public void setEventType(Kind type) {
        this.typeCode = type;
    }

    public BaseNode getSourceNode() {
        return this.sourceNode;
    }
    
    public void setSourceNode(BaseNode node) {
        this.sourceNode = node;
    }
    
    public Fact[] getFacts() {
        return this.facts;
    }
    
    public void setFacts(Fact[] facts) {
        this.facts = facts;
    }
}
