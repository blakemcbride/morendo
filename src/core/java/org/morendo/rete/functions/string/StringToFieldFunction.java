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

/**
 * {@code (string-to-field <string>)}: reads the first field of the text as the parser would: a
 * number, a quoted string, TRUE/FALSE, nil, or a symbol.
 */
public class StringToFieldFunction implements Function {

    public static final String STRING_TO_FIELD = "string-to-field";

    public ValueType getReturnType() {
        return ValueType.OBJECT;
    }

    public ReturnVector executeFunction(Rete engine, Parameter[] params) {
        Object value = Boolean.FALSE;
        if (params != null && params.length == 1) {
            Object arg = params[0].getValue(engine, ValueType.STRING);
            value = field(arg == null ? "" : arg.toString());
        }
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.OBJECT, value));
        return ret;
    }

    /** The first field of the text, converted; the empty string for no field. */
    public static Object field(String text) {
        String s = text.strip();
        if (s.isEmpty()) {
            return "";
        }
        if (s.charAt(0) == '"') {
            int end = s.indexOf('"', 1);
            while (end > 0 && s.charAt(end - 1) == '\\') {
                end = s.indexOf('"', end + 1);
            }
            String body = end < 0 ? s.substring(1) : s.substring(1, end);
            return body.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        int space = 0;
        while (space < s.length() && !Character.isWhitespace(s.charAt(space))) {
            space++;
        }
        String token = s.substring(0, space);
        if ("TRUE".equals(token) || "true".equals(token)) {
            return Boolean.TRUE;
        } else if ("FALSE".equals(token) || "false".equals(token)) {
            return Boolean.FALSE;
        } else if ("nil".equals(token)) {
            return null;
        }
        if (token.matches("[-+]?(\\d+\\.?\\d*|\\.\\d+)([eE][-+]?\\d+)?")) {
            try {
                return new BigDecimal(token);
            } catch (NumberFormatException e) {
                return token;
            }
        }
        return token;
    }

    public String getName() {
        return STRING_TO_FIELD;
    }

    public Class<?>[] getParameter() {
        return new Class<?>[] {String.class};
    }

    public String toPPString(Parameter[] params, int indents) {
        return "(string-to-field <string>)\n the first field of the text as a number, string,"
                + " boolean, nil or symbol.";
    }
}
