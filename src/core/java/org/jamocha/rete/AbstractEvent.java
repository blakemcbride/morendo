/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://jamocha.sourceforge.net/
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
 */
public abstract class AbstractEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    /** */

    /** What a compile event reports. */
    public enum Kind {
        ADD_RULE,
        REMOVE_RULE,
        PARSE_ERROR,
        INVALID_RULE,
        RULE_EXISTS,
        TEMPLATE_NOTFOUND,
        CLIPSPARSER_ERROR,
        CLIPSPARSER_WARNING,
        CLIPSPARSER_REINIT,
        FUNCTION_NOT_FOUND,
        FUNCTION_INVALID,
        ADD_NODE_ERROR
    }

    /**
     * @param source
     */
    public AbstractEvent(Object source) {
        super(source);
    }
}
