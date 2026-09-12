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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;

/**
 * {@code (system <value>+)}: runs the arguments, joined with spaces, as a command of the operating
 * system's shell, prints its output and returns its exit code.
 */
public class SystemFunction implements Function {

    public static final String SYSTEM = "system";

    public ValueType getReturnType() {
        return ValueType.BIG_DECIMAL;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object result = Boolean.FALSE;
        if (params != null && params.length > 0) {
            StringBuilder command = new StringBuilder();
            for (Parameter param : params) {
                if (command.length() > 0) {
                    command.append(' ');
                }
                command.append(param.getValue(engine, ValueType.STRING));
            }
            result = run(engine, command.toString());
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(
                new DefaultReturnValue(
                        result instanceof BigDecimal
                                ? ValueType.BIG_DECIMAL
                                : ValueType.BOOLEAN_OBJECT,
                        result));
        return ret;
    }

    private static Object run(Rete engine, String command) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
        ProcessBuilder builder =
                windows
                        ? new ProcessBuilder("cmd", "/c", command)
                        : new ProcessBuilder("sh", "-c", command);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    engine.writeMessage(line + System.lineSeparator());
                }
            }
            return BigDecimal.valueOf(process.waitFor());
        } catch (IOException e) {
            engine.writeMessage("system: " + e.getMessage() + System.lineSeparator());
            return Boolean.FALSE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Boolean.FALSE;
        }
    }

    public String getName() {
        return SYSTEM;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {Object[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(system <value>+)\n runs a command of the operating system's shell, prints its"
                + " output and returns the exit code.";
    }
}
