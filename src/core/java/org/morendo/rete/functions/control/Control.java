package org.morendo.rete.functions.control;

import org.morendo.rete.BoundParam;
import org.morendo.rete.DefaultReturnValue;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.FunctionParam2;
import org.morendo.rete.Parameter;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnValue;
import org.morendo.rete.ReturnVector;
import org.morendo.rete.ValueParam;
import org.morendo.rete.ValueType;

import java.math.BigDecimal;
import java.util.List;

/** Shared evaluation helpers for the control functions. */
final class Control {

    /** -Dmorendo.loop.limit=N stops any single loop after N turns; 0 or unset means no limit. */
    static final long LOOP_LIMIT = Long.getLong("morendo.loop.limit", 0L);

    private Control() {}

    /** Evaluates one argument: a nested call runs, a variable is looked up, a literal is itself. */
    static Object eval(Rete engine, Parameter param) {
        if (param instanceof FunctionParam2 call) {
            call.setEngine(engine);
            return call.evaluate();
        } else if (param instanceof BoundParam bp) {
            return bp.getValue(engine, ValueType.OBJECT);
        } else if (param instanceof ValueParam vp) {
            return vp.getValue();
        }
        return param == null ? null : param.getValue();
    }

    /** True for TRUE/true, a non-null non-false value, and a return vector whose first value is. */
    static boolean isTrue(Object value) {
        if (value instanceof ReturnVector rv) {
            return rv.size() > 0 && rv.firstReturnValue().getBooleanValue();
        } else if (value instanceof ReturnValue rval) {
            return rval.getBooleanValue();
        } else if (value instanceof Boolean b) {
            return b;
        } else if (value instanceof String s) {
            return !("false".equalsIgnoreCase(s) || "nil".equals(s) || s.isEmpty());
        }
        return value != null;
    }

    /** Runs the body arguments in order; a break or return unwinds through here. */
    static Object runBody(Rete engine, Parameter[] params, int from) {
        Object last = null;
        for (int idx = from; idx < params.length; idx++) {
            last = eval(engine, params[idx]);
        }
        return last;
    }

    /** True when the argument is the keyword given, written as a bare symbol. */
    static boolean isKeyword(Parameter param, String keyword) {
        return param instanceof ValueParam vp && keyword.equals(vp.getStringValue());
    }

    /** The variable name of a ?x argument, without the question mark. */
    static String variableName(Parameter param) {
        if (param instanceof BoundParam bp) {
            return bp.getVariableName();
        }
        String text = String.valueOf(param.getStringValue());
        return text.startsWith("?") ? text.substring(1) : text;
    }

    /** The elements of a list value: an array, a collection, or a single value. */
    static Object[] elements(Object value) {
        if (value instanceof Object[] array) {
            return array;
        } else if (value instanceof List<?> list) {
            return list.toArray();
        } else if (value == null) {
            return new Object[0];
        }
        return new Object[] {value};
    }

    static long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value).trim());
    }

    static BigDecimal number(long value) {
        return BigDecimal.valueOf(value);
    }

    static ReturnVector result(Object value) {
        DefaultReturnVector ret = new DefaultReturnVector();
        ret.addReturnValue(new DefaultReturnValue(ValueType.OBJECT, value));
        return ret;
    }

    static void checkLimit(long turns) {
        if (LOOP_LIMIT > 0 && turns > LOOP_LIMIT) {
            throw new IllegalStateException(
                    "loop stopped after " + LOOP_LIMIT + " turns (morendo.loop.limit)");
        }
    }
}
