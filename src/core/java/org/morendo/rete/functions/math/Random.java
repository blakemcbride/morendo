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

import java.math.BigDecimal;

/**
 * {@code (random)} is a pseudo-random double in [0, 1); {@code (random <start> <end>)} a
 * pseudo-random integer between the bounds inclusive. The generator belongs to the engine and
 * {@code seed} restarts it.
 */
public class Random implements Function {

    public static final String RANDOM = "random";

    private java.util.Random generator = new java.util.Random();

    public void seed(long seed) {
        this.generator = new java.util.Random(seed);
    }

    public ValueType getReturnType() {
        return ValueType.DOUBLE_PRIM;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        DefaultReturnVector ret = new DefaultReturnVector();
        if (params != null && params.length == 2) {
            long start = Div.number(engine, params[0]).longValue();
            long end = Div.number(engine, params[1]).longValue();
            long low = Math.min(start, end);
            long high = Math.max(start, end);
            long value = low + (long) (this.generator.nextDouble() * (high - low + 1));
            ret.addReturnValue(
                    new DefaultReturnValue(
                            ValueType.BIG_DECIMAL, BigDecimal.valueOf(Math.min(value, high))));
        } else {
            ret.addReturnValue(
                    new DefaultReturnValue(ValueType.DOUBLE_PRIM, this.generator.nextDouble()));
        }
        return ret;
    }

    public String getName() {
        return RANDOM;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {BigDecimal.class, BigDecimal.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(random [<start> <end>])\n a pseudo-random double in [0,1), or an integer between"
                + " start and end inclusive.";
    }
}
