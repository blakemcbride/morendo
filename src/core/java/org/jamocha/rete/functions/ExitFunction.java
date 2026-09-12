/*
 * Copyright 2002-2009 Peter Lin
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
package org.jamocha.rete.functions;

import org.jamocha.rete.Function;
import org.jamocha.rete.Parameter;
import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;
import org.jamocha.rete.ValueType;

/**
 * @author Peter Lin
 *     <p>ExitFunction closes the engine. The shell ends when it sees the engine closed, and an
 *     application can register a close hook on the engine if it wants to end as well.
 */
public class ExitFunction implements Function {

    /** */
    public static final String EXIT = "exit";

    /** */
    public ExitFunction() {
        super();
    }

    public ValueType getReturnType() {
        return ValueType.RETURN_VOID;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        if (engine != null) {
            engine.close();
        }
        return null;
    }

    public String getName() {
        return EXIT;
    }

    /**
     * the function does not take any parameters. If parameters are passed, the function ignores
     * them.
     */
    public Class<?>[] getParameter() {
        return new Class<?>[0];
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(exit)";
    }
}
