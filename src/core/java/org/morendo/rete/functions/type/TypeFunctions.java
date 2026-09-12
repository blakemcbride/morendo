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
package org.morendo.rete.functions.type;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

/** The type tests and conversions of CLIPS. */
public class TypeFunctions implements FunctionGroup {

    public static final String NAME = "Type Functions";

    private final List<Function> funcs = new ArrayList<>();

    public String getName() {
        return NAME;
    }

    public List<Function> listFunctions() {
        return funcs;
    }

    private void declare(Rete engine, Function f) {
        engine.declareFunction(f);
        funcs.add(f);
    }

    public void loadFunctions(Rete engine) {
        declare(
                engine,
                new TypePredicateFunction(
                        "numberp",
                        "true for an integer or floating point value.",
                        v -> v instanceof Number));
        declare(
                engine,
                new TypePredicateFunction(
                        "integerp",
                        "true for an integer value.",
                        TypePredicateFunction::isInteger));
        declare(
                engine,
                new TypePredicateFunction(
                        "floatp",
                        "true for a floating point value.",
                        TypePredicateFunction::isFloat));
        declare(
                engine,
                new TypePredicateFunction(
                        "stringp",
                        "true for text; strings and symbols are the same type.",
                        v -> v instanceof String));
        declare(
                engine,
                new TypePredicateFunction(
                        "symbolp",
                        "true for text; strings and symbols are the same type.",
                        v -> v instanceof String));
        declare(
                engine,
                new TypePredicateFunction(
                        "lexemep", "true for a string or symbol.", v -> v instanceof String));
        declare(
                engine,
                new TypePredicateFunction(
                        "multifieldp",
                        "true for a list.",
                        v -> v instanceof Object[] || v instanceof java.util.List));
        declare(engine, new NumberConversionFunction(true));
        declare(engine, new NumberConversionFunction(false));
    }
}
