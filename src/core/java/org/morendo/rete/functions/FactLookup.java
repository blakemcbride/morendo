/*
 * Copyright 2026 Blake McBride
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.functions;

import org.morendo.rete.BoundParam;
import org.morendo.rete.Fact;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ValueType;

/**
 * Resolves the fact argument of the fact functions: a fact bound on a rule's left-hand side, a
 * shell variable holding the id an assert returned, a literal id, or a fact value.
 */
final class FactLookup {

    private FactLookup() {}

    static Fact find(Rete engine, Parameter param) {
        Object value;
        if (param instanceof BoundParam bp) {
            if (bp.getFact() != null) {
                return bp.getFact();
            }
            value = engine.getBinding(bp.getVariableName());
        } else {
            value = param.getValue(engine, ValueType.OBJECT);
        }
        if (value instanceof Fact fact) {
            return fact;
        } else if (value instanceof Number n) {
            return engine.getFactById(n.longValue());
        } else if (value != null) {
            Fact shadow = engine.getShadowFact(value);
            if (shadow != null) {
                return shadow;
            }
            try {
                return engine.getFactById(Long.parseLong(value.toString().trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
