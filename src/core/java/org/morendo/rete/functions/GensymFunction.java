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

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

/**
 * {@code (gensym)} returns a new symbol {@code gen1}, {@code gen2}, ...; {@code (gensym*)} the same
 * but skipping names that are already in use as a bound variable. The counter belongs to the
 * engine's gensym function and {@code setgen} sets it.
 */
public class GensymFunction implements Function {

    public static final String GENSYM = "gensym";
    public static final String GENSYM_STAR = "gensym*";

    private final boolean unique;
    private long counter = 1;

    public GensymFunction(boolean unique) {
        this.unique = unique;
    }

    public void setCounter(long value) {
        this.counter = value;
    }

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        GensymFunction shared = engine.findFunction(GENSYM) instanceof GensymFunction g ? g : this;
        String symbol = "gen" + shared.counter++;
        if (this.unique) {
            while (engine.getBinding(symbol) != null) {
                symbol = "gen" + shared.counter++;
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.STRING, symbol));
        return ret;
    }

    public String getName() {
        return this.unique ? GENSYM_STAR : GENSYM;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return this.unique
                ? "(gensym*)\n a new symbol genN not in use as a variable name."
                : "(gensym)\n a new symbol genN.";
    }
}
