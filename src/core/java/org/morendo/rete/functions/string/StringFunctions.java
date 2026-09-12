/*
 * Copyright 2002-2006 Peter Lin
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
package org.morendo.rete.functions.string;

import org.morendo.rete.Function;
import org.morendo.rete.FunctionGroup;
import org.morendo.rete.Rete;

import java.util.ArrayList;
import java.util.List;

public class StringFunctions implements FunctionGroup {

    /** */
    private ArrayList<Function> funcs = new ArrayList<>();

    public StringFunctions() {
        super();
    }

    public String getName() {
        return (StringFunctions.class.getSimpleName());
    }

    public void loadFunctions(Rete engine) {
        StringCompareFunction compare = new StringCompareFunction();
        engine.declareFunction(compare);
        funcs.add(compare);
        StringContainsFunction cont = new StringContainsFunction();
        engine.declareFunction(cont);
        funcs.add(cont);
        StringIndexFunction indx = new StringIndexFunction();
        engine.declareFunction(indx);
        funcs.add(indx);
        StringLengthFunction strlen = new StringLengthFunction();
        engine.declareFunction(strlen);
        funcs.add(strlen);
        StringLowerFunction lower = new StringLowerFunction();
        engine.declareFunction(lower);
        funcs.add(lower);
        StringReplaceFunction strrepl = new StringReplaceFunction();
        engine.declareFunction(strrepl);
        funcs.add(strrepl);
        StringUpperFunction upper = new StringUpperFunction();
        engine.declareFunction(upper);
        funcs.add(upper);
        SubStringFunction sub = new SubStringFunction();
        engine.declareFunction(sub);
        funcs.add(sub);
        StringTrimFunction trim = new StringTrimFunction();
        engine.declareFunction(trim);
        funcs.add(trim);
        StringNotEmptyFunction snef = new StringNotEmptyFunction();
        engine.declareFunction(snef);
        try {
            engine.declareFunction("upcase", upper);
            engine.declareFunction("lowcase", lower);
        } catch (org.morendo.rete.exception.FunctionException e) {
            throw new IllegalStateException(e);
        }
        for (Function f :
                new Function[] {
                    new FormatFunction(), new SymCatFunction(), new StringToFieldFunction()
                }) {
            engine.declareFunction(f);
            funcs.add(f);
        }
        StrCatFunction strcat = new StrCatFunction();
        funcs.add(strcat);
        engine.declareFunction(strcat);
        funcs.add(snef);
    }

    public List<Function> listFunctions() {
        return funcs;
    }
}
