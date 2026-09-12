/*
 * Copyright 2002-2008 Peter Lin
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
package org.morendo.rete;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * Compares slot values for the alpha and join nodes.
 *
 * <p>Rules of comparison: NIL equals null and nothing else; strings and booleans compare by their
 * text (a boolean against a string compares "true"/"false"); numbers compare as longs when both are
 * integral and as doubles otherwise, and compare equal to a string holding their text; instants,
 * dates and calendars compare by epoch millisecond, also against numbers; anything else falls back
 * to equals(). Ordering is defined for numbers and temporal values only.
 */
public class Evaluate {

    public static boolean evaluate(Operator operator, Object left, Object right) {
        return switch (operator) {
            case EQUAL -> evaluateEqual(left, right);
            case NOTEQUAL -> evaluateNotEqual(left, right);
            case LESS -> evaluateLess(left, right);
            case LESSEQUAL -> evaluateLessEqual(left, right);
            case GREATER -> evaluateGreater(left, right);
            case GREATEREQUAL -> evaluateGreaterEqual(left, right);
            case NILL -> evaluateNull(left, right);
            default -> false;
        };
    }

    public static boolean evaluateEqual(Object left, Object right) {
        if (Constants.NIL_SYMBOL.equals(left)) {
            return right == null;
        }
        return switch (left) {
            case null -> false;
            case String s -> evaluateStringEqual(s, right);
            case Boolean b -> evaluateBooleanEqual(b, right);
            case Number n ->
                    right instanceof String s
                            ? n.toString().equals(s)
                            : compare(Operator.EQUAL, n, right);
            case Object o when isTemporal(o) -> evaluateDateEqual(temporalMillis(o), right);
            default -> right != null && left.equals(right);
        };
    }

    public static boolean evaluateNotEqual(Object left, Object right) {
        if (Constants.NIL_SYMBOL.equals(left)) {
            return right != null;
        }
        return switch (left) {
            case null -> false;
            case String s -> !s.equals(right);
            case Boolean b -> evaluateBooleanNotEqual(b, right);
            case Number n ->
                    right instanceof String s
                            ? !n.toString().equals(s)
                            : compare(Operator.NOTEQUAL, n, right);
            case Object o when isTemporal(o) -> evaluateDateNotEqual(temporalMillis(o), right);
            default -> right != null && !left.equals(right);
        };
    }

    public static boolean evaluateLess(Object left, Object right) {
        return order(Operator.LESS, left, right);
    }

    public static boolean evaluateLessEqual(Object left, Object right) {
        return order(Operator.LESSEQUAL, left, right);
    }

    public static boolean evaluateGreater(Object left, Object right) {
        return order(Operator.GREATER, left, right);
    }

    public static boolean evaluateGreaterEqual(Object left, Object right) {
        return order(Operator.GREATEREQUAL, left, right);
    }

    public static boolean evaluateNull(Object left, Object right) {
        return right == null;
    }

    public static boolean evaluateStringEqual(String left, Object right) {
        return right instanceof Boolean ? left.equals(right.toString()) : left.equals(right);
    }

    public static boolean evaluateBooleanEqual(Boolean left, Object right) {
        return switch (right) {
            case Boolean b -> left.equals(b);
            case String s -> left.toString().equals(s);
            case null, default -> false;
        };
    }

    public static boolean evaluateBooleanNotEqual(Boolean left, Object right) {
        return switch (right) {
            case Boolean b -> !left.equals(b);
            case String s -> !left.equals(Boolean.valueOf(s));
            case null, default -> false;
        };
    }

    /**
     * Ordering comparisons: numbers against numbers, temporal values against temporal values or
     * numbers.
     */
    private static boolean order(Operator operator, Object left, Object right) {
        return switch (left) {
            case Number n -> compare(operator, n, right);
            case Object o when isTemporal(o) -> compareMillis(operator, temporalMillis(o), right);
            case null, default -> false;
        };
    }

    /**
     * Compares a number with a right-hand value that must also be a number: exactly as longs when
     * both are integral, as doubles when a floating-point or decimal value is involved.
     */
    private static boolean compare(Operator operator, Number left, Object right) {
        if (!(right instanceof Number r)) {
            return false;
        }
        if (isIntegral(left) && isIntegral(r)) {
            return test(operator, Long.compare(left.longValue(), r.longValue()));
        }
        double l = left.doubleValue();
        double d = r.doubleValue();
        return switch (operator) {
            case EQUAL -> l == d;
            case NOTEQUAL -> l != d;
            case LESS -> l < d;
            case LESSEQUAL -> l <= d;
            case GREATER -> l > d;
            case GREATEREQUAL -> l >= d;
            default -> false;
        };
    }

    private static boolean isIntegral(Number n) {
        return n instanceof Integer
                || n instanceof Long
                || n instanceof Short
                || n instanceof Byte
                || n instanceof BigInteger;
    }

    /** Applies an operator to the sign of a comparison result. */
    private static boolean test(Operator operator, int comparison) {
        return switch (operator) {
            case EQUAL -> comparison == 0;
            case NOTEQUAL -> comparison != 0;
            case LESS -> comparison < 0;
            case LESSEQUAL -> comparison <= 0;
            case GREATER -> comparison > 0;
            case GREATEREQUAL -> comparison >= 0;
            default -> false;
        };
    }

    // ---------------------------------------------------------------- temporal values

    /**
     * True for the values a DATE slot or a time function may hold: an Instant, or a legacy Date or
     * Calendar from a bean.
     */
    public static boolean isTemporal(Object value) {
        return value instanceof Instant || value instanceof Date || value instanceof Calendar;
    }

    /** Epoch milliseconds of a temporal value or a number; Long.MIN_VALUE for anything else. */
    public static long temporalMillis(Object value) {
        return switch (value) {
            case Instant i -> i.toEpochMilli();
            case Date d -> d.getTime();
            case Calendar c -> c.getTimeInMillis();
            case Number n -> n.longValue();
            case null, default -> Long.MIN_VALUE;
        };
    }

    private static boolean compareMillis(Operator operator, long left, Object right) {
        return (isTemporal(right) || right instanceof Number)
                && test(operator, Long.compare(left, temporalMillis(right)));
    }

    public static boolean evaluateDateEqual(long left, Object right) {
        return compareMillis(Operator.EQUAL, left, right);
    }

    public static boolean evaluateDateNotEqual(long left, Object right) {
        return compareMillis(Operator.NOTEQUAL, left, right);
    }

    public static boolean evaluateDateLess(long left, Object right) {
        return compareMillis(Operator.LESS, left, right);
    }

    public static boolean evaluateDateLessEqual(long left, Object right) {
        return compareMillis(Operator.LESSEQUAL, left, right);
    }

    public static boolean evaluateDateGreater(long left, Object right) {
        return compareMillis(Operator.GREATER, left, right);
    }

    public static boolean evaluateDateGreaterEqual(long left, Object right) {
        return compareMillis(Operator.GREATEREQUAL, left, right);
    }

    // ---------------------------------------------------------------- facts

    /** True when both arrays hold the same fact instances in the same order. */
    public static boolean factsEqual(Fact[] left, Fact[] right) {
        if (left == right) {
            return true;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (left[i] != right[i]) {
                return false;
            }
        }
        return true;
    }
}
