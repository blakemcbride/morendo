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
package org.morendo.rete.functions.string;

import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Function;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code (format <router> <format-string> <argument>*)}: formats with the CLIPS directives {@code
 * %d %f %e %g %s %c %n %%} (widths and precisions as in C), writes the text to the router unless it
 * is {@code nil}, and returns it.
 */
public class FormatFunction implements Function {

    public static final String FORMAT = "format";

    private static final Pattern DIRECTIVE =
            Pattern.compile("%([-+ 0#]*)(\\d*)(?:\\.(\\d+))?([dfegscn%])");

    public ValueType getReturnType() {
        return ValueType.STRING;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        String text = "";
        if (params != null && params.length >= 2) {
            Object router = params[0].getValue(engine, ValueType.STRING);
            String pattern = String.valueOf(params[1].getValue(engine, ValueType.STRING));
            Object[] args = new Object[params.length - 2];
            for (int i = 0; i < args.length; i++) {
                args[i] = params[i + 2].getValue(engine, ValueType.OBJECT);
            }
            text = format(pattern, args);
            if (router != null && !"nil".equals(router)) {
                engine.writeMessage(text, router.toString());
            }
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.STRING, text));
        return ret;
    }

    /** Translates the directives to {@link String#format} and coerces each argument to its type. */
    static String format(String pattern, Object[] args) {
        StringBuilder java = new StringBuilder();
        List<Object> coerced = new ArrayList<>();
        Matcher m = DIRECTIVE.matcher(pattern);
        int last = 0;
        int next = 0;
        while (m.find()) {
            java.append(pattern, last, m.start());
            last = m.end();
            char conversion = m.group(4).charAt(0);
            if (conversion == '%' || conversion == 'n') {
                java.append('%').append(conversion);
                continue;
            }
            Object arg = next < args.length ? args[next++] : null;
            String flags = m.group(1);
            String width = m.group(2);
            String precision = m.group(3) == null ? "" : "." + m.group(3);
            switch (conversion) {
                case 'd' -> {
                    coerced.add(arg instanceof Number n ? n.longValue() : parseLong(arg));
                    java.append('%').append(flags).append(width).append('d');
                }
                case 'f', 'e', 'g' -> {
                    coerced.add(arg instanceof Number n ? n.doubleValue() : parseDouble(arg));
                    java.append('%')
                            .append(flags)
                            .append(width)
                            .append(precision)
                            .append(conversion);
                }
                case 'c' -> {
                    coerced.add(character(arg));
                    java.append('%').append(flags).append(width).append('c');
                }
                default -> {
                    coerced.add(arg == null ? "nil" : text(arg));
                    java.append('%').append(flags).append(width).append(precision).append('s');
                }
            }
        }
        java.append(pattern, last, pattern.length());
        try {
            return String.format(java.toString(), coerced.toArray());
        } catch (IllegalFormatException e) {
            return "format error: " + e.getMessage();
        }
    }

    private static String text(Object value) {
        if (value instanceof Object[] list) {
            StringBuilder buf = new StringBuilder();
            for (Object v : list) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                buf.append(v);
            }
            return buf.toString();
        }
        return value.toString();
    }

    private static long parseLong(Object arg) {
        try {
            return new BigDecimal(String.valueOf(arg).trim()).longValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(Object arg) {
        try {
            return Double.parseDouble(String.valueOf(arg).trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static char character(Object arg) {
        if (arg instanceof Number n) {
            return (char) n.intValue();
        }
        String s = String.valueOf(arg);
        return s.isEmpty() ? ' ' : s.charAt(0);
    }

    public String getName() {
        return FORMAT;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class, String.class, Object[].class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(format <router> <format-string> <argument>*)\n formats with %d %f %e %g %s %c"
                + " %n; writes to the router unless it is nil, and returns the text.";
    }
}
