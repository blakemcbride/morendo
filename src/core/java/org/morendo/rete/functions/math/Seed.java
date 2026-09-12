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
package org.morendo.rete.functions.math;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/** {@code (seed <integer>)}: restarts the engine's random number generator. */
public class Seed implements Function {

    public static final String SEED = "seed";

    public ValueType getReturnType() {
        return ValueType.BOOLEAN_OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        boolean done = false;
        if (params != null
                && params.length == 1
                && engine.findFunction(Random.RANDOM) instanceof Random random) {
            random.seed(Div.number(engine, params[0]).longValue());
            done = true;
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.BOOLEAN_OBJECT, done));
        return ret;
    }

    public String getName() {
        return SEED;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Long.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(seed <integer>)\n restarts the random number generator.";
    }
}
