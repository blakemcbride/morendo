/*
 * Copyright 2002-2008 Peter Lin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://jamocha.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Peter Lin
 *     <p>ConversioUtils has a methods for autoboxing primitive types with the Object equivalent.
 */
public class ConversionUtils {

    private static HashMap<String, String> OPR_MAP;

    static {
        OPR_MAP = new HashMap<>();
        OPR_MAP.put(String.valueOf(Operator.ADD), Constants.ADD_STRING);
        OPR_MAP.put(String.valueOf(Operator.SUBTRACT), Constants.SUBTRACT_STRING);
        OPR_MAP.put(String.valueOf(Operator.MULTIPLY), Constants.MULTIPLY_STRING);
        OPR_MAP.put(String.valueOf(Operator.DIVIDE), Constants.DIVIDE_STRING);
        OPR_MAP.put(String.valueOf(Operator.LESS), Constants.LESS_STRING);
        OPR_MAP.put(String.valueOf(Operator.LESSEQUAL), Constants.LESSEQUAL_STRING);
        OPR_MAP.put(String.valueOf(Operator.GREATER), Constants.GREATER_STRING);
        OPR_MAP.put(String.valueOf(Operator.GREATEREQUAL), Constants.GREATEREQUAL_STRING);
        OPR_MAP.put(String.valueOf(Operator.EQUAL), Constants.EQUAL_STRING);
        OPR_MAP.put(String.valueOf(Operator.NOTEQUAL), Constants.NOTEQUAL_STRING);
    }

    @SuppressWarnings("unused")
    private static HashMap<?, ?> STROPR_MAP = null;

    static {
        STROPR_MAP = new HashMap<>();
        OPR_MAP.put(Constants.ADD_STRING, Constants.ADD_SYMBOL);
        OPR_MAP.put(Constants.SUBTRACT_STRING, Constants.SUBTRACT_SYMBOL);
        OPR_MAP.put(Constants.MULTIPLY_STRING, Constants.MULTIPLY_SYMBOL);
        OPR_MAP.put(Constants.DIVIDE_STRING, Constants.DIVIDE_SYMBOL);
        OPR_MAP.put(Constants.LESS_STRING, Constants.LESS_SYMBOL);
        OPR_MAP.put(Constants.LESSEQUAL_STRING, Constants.LESSEQUAL_SYMBOL);
        OPR_MAP.put(Constants.GREATER_STRING, Constants.GREATER_SYMBOL);
        OPR_MAP.put(Constants.GREATEREQUAL_STRING, Constants.GREATEREQUAL_SYMBOL);
        OPR_MAP.put(Constants.EQUAL_STRING, Constants.EQUAL_SYMBOL);
        OPR_MAP.put(Constants.NOTEQUAL_STRING, Constants.NOTEQUAL_SYMBOL);
    }

    /**
     * Convert a int primitive to an Integer object
     *
     * @param val
     * @return
     */
    public static Object convert(int val) {
        return Integer.valueOf(val);
    }

    /**
     * Convert a short primitive to a Short object
     *
     * @param val
     * @return
     */
    public static Object convert(short val) {
        return Short.valueOf(val);
    }

    /**
     * Convert a float primitive to Float object
     *
     * @param val
     * @return
     */
    public static Object convert(float val) {
        return Float.valueOf(val);
    }

    /**
     * convert a primitive long to Long object
     *
     * @param val
     * @return
     */
    public static Object convert(long val) {
        return Long.valueOf(val);
    }

    /**
     * convert a primitive double to a Double object
     *
     * @param val
     * @return
     */
    public static Object convert(double val) {
        return Double.valueOf(val);
    }

    /**
     * convert a primitive byte to Byte object
     *
     * @param val
     * @return
     */
    public static Object convert(byte val) {
        return Byte.valueOf(val);
    }

    public static Object convert(ValueType type, Object val) {
        if (type == ValueType.INT_PRIM || type == ValueType.INTEGER_OBJECT) {
            if (val instanceof BigDecimal bigDecimal) {
                return Integer.valueOf((bigDecimal).intValue());
            }
        } else if (type == ValueType.SHORT_PRIM || type == ValueType.SHORT_OBJECT) {
            if (val instanceof BigDecimal bigDecimalValue) {
                return Short.valueOf((bigDecimalValue).shortValue());
            }
        } else if (type == ValueType.FLOAT_PRIM || type == ValueType.FLOAT_OBJECT) {
            if (val instanceof BigDecimal) {
                return Float.valueOf(((BigDecimal) val).floatValue());
            }
        } else if (type == ValueType.LONG_PRIM || type == ValueType.LONG_OBJECT) {
            if (val instanceof BigDecimal) {
                return Long.valueOf(((BigDecimal) val).longValue());
            }
        } else if (type == ValueType.DOUBLE_PRIM || type == ValueType.DOUBLE_OBJECT) {
            if (val instanceof BigDecimal) {
                return Double.valueOf(((BigDecimal) val).doubleValue());
            }
        }
        return val;
    }

    /**
     * Return the string form of the operator
     *
     * @param opr
     * @return
     */
    public static String getPPOperator(Operator opr) {
        return OPR_MAP.get(String.valueOf(opr));
    }

    /**
     * find the matching fact in the array
     *
     * @param temp
     * @param facts
     * @return
     */
    public static Fact findFact(Deftemplate temp, Fact[] facts) {
        Fact ft = null;
        for (int idx = 0; idx < facts.length; idx++) {
            if (facts[idx].getDeftemplate() == temp) {
                ft = facts[idx];
            }
        }
        return ft;
    }

    /**
     * Method will merge the two arrays by add the facts from the right to the end
     *
     * @param left
     * @param right
     * @return
     */
    public static Fact[] mergeFacts(Fact[] left, Fact[] right) {
        Fact[] merged = new Fact[left.length + right.length];
        System.arraycopy(left, 0, merged, 0, left.length);
        System.arraycopy(right, 0, merged, left.length, right.length);
        return merged;
    }

    /**
     * The method will merge a single right fact with the left fact array.
     *
     * @param left
     * @param right
     * @return
     */
    public static Fact[] mergeFacts(Fact[] left, Fact right) {
        Fact[] merged = new Fact[left.length + 1];
        System.arraycopy(left, 0, merged, 0, left.length);
        merged[left.length] = right;
        return merged;
    }

    /**
     * Add a new object to an object array
     *
     * @param list
     * @param nobj
     * @return
     */
    public static BaseNode[] add(BaseNode[] list, BaseNode nobj) {
        BaseNode[] newlist = new BaseNode[list.length + 1];
        System.arraycopy(list, 0, newlist, 0, list.length);
        newlist[list.length] = nobj;
        return newlist;
    }

    /**
     * remove an object from an object array
     *
     * @param list
     * @param nobj
     * @return
     */
    public static BaseNode[] remove(BaseNode[] list, Object nobj) {
        int found = 0;
        for (BaseNode node : list) {
            if (node == nobj) {
                found++;
            }
        }
        if (found == 0) {
            // not a member: the list is unchanged
            return list;
        }
        BaseNode[] newlist = new BaseNode[list.length - found];
        int pos = 0;
        for (int idx = 0; idx < list.length; idx++) {
            if (list[idx] != nobj) {
                newlist[pos] = list[idx];
                pos++;
            }
        }
        return newlist;
    }

    /**
     * Return the int mapped type for the field
     *
     * @param clzz
     * @return
     */
    public static ValueType getTypeCode(Class<?> clzz) {
        if (clzz.isArray()) {
            return ValueType.ARRAY;
        } else if (clzz.isPrimitive()) {
            if (clzz == int.class) {
                return ValueType.INT_PRIM;
            } else if (clzz == short.class) {
                return ValueType.SHORT_PRIM;
            } else if (clzz == long.class) {
                return ValueType.LONG_PRIM;
            } else if (clzz == float.class) {
                return ValueType.FLOAT_PRIM;
            } else if (clzz == byte.class) {
                return ValueType.BYTE_PRIM;
            } else if (clzz == double.class) {
                return ValueType.DOUBLE_PRIM;
            } else if (clzz == boolean.class) {
                return ValueType.BOOLEAN_PRIM;
            } else if (clzz == char.class) {
                return ValueType.CHAR_PRIM;
            } else {
                return ValueType.OBJECT;
            }
        } else if (clzz == Date.class || clzz == Instant.class) {
            return ValueType.DATE;
        } else if (clzz == String.class) {
            return ValueType.STRING;
        } else {
            return ValueType.OBJECT;
        }
    }

    /**
     * Convienance method for converting the ValueType type code to the string form
     *
     * @param intType
     * @return
     */
    public static String getTypeName(ValueType intType) {
        if (intType == ValueType.INT_PRIM) {
            return "INTEGER";
        } else if (intType == ValueType.SHORT_PRIM) {
            return "SHORT";
        } else if (intType == ValueType.LONG_PRIM) {
            return "LONG";
        } else if (intType == ValueType.FLOAT_PRIM) {
            return "FLOAT";
        } else if (intType == ValueType.DOUBLE_PRIM) {
            return "DOUBLE";
        } else if (intType == ValueType.BYTE_PRIM) {
            return "BYTE";
        } else if (intType == ValueType.BOOLEAN_PRIM) {
            return "BOOLEAN";
        } else if (intType == ValueType.CHAR_PRIM) {
            return "CHAR";
        } else if (intType == ValueType.STRING) {
            return "STRING";
        } else if (intType == ValueType.DATE) {
            return "DATE";
        } else if (intType == ValueType.ARRAY) {
            return Object[].class.getName();
        } else {
            return Object.class.getName();
        }
    }

    public static Operator getOperatorCode(String strSymbol) {
        if (strSymbol.equals(Constants.EQUAL_SYMBOL)) {
            return Operator.EQUAL;
        } else if (strSymbol.equals(Constants.NOTEQUAL_SYMBOL)) {
            return Operator.NOTEQUAL;
        } else if (strSymbol.equals(Constants.ADD_SYMBOL)) {
            return Operator.ADD;
        } else if (strSymbol.equals(Constants.SUBTRACT_SYMBOL)) {
            return Operator.SUBTRACT;
        } else if (strSymbol.equals(Constants.MULTIPLY_SYMBOL)) {
            return Operator.MULTIPLY;
        } else if (strSymbol.equals(Constants.DIVIDE_SYMBOL)) {
            return Operator.DIVIDE;
        } else if (strSymbol.equals(Constants.GREATER_SYMBOL)) {
            return Operator.GREATER;
        } else if (strSymbol.equals(Constants.GREATEREQUAL_SYMBOL)) {
            return Operator.GREATEREQUAL;
        } else if (strSymbol.equals(Constants.LESS_SYMBOL)) {
            return Operator.LESS;
        } else if (strSymbol.equals(Constants.LESSEQUAL_SYMBOL)) {
            return Operator.LESSEQUAL;
        } else {
            return Operator.USERDEFINED;
        }
    }

    public static Operator getOppositeOperatorCode(Operator op) {
        Operator rvop = Operator.EQUAL;
        switch (op) {
            case EQUAL:
                rvop = Operator.NOTEQUAL;
                break;
            case NOTEQUAL:
                rvop = Operator.EQUAL;
                break;
            case GREATER:
                rvop = Operator.LESS;
                break;
            case LESS:
                rvop = Operator.GREATER;
                break;
            case GREATEREQUAL:
                rvop = Operator.LESSEQUAL;
                break;
            case LESSEQUAL:
                rvop = Operator.GREATEREQUAL;
                break;
        }
        return rvop;
    }

    public static String getOppositeOperator(String strSymbol) {
        if (strSymbol.equals(Constants.EQUAL_SYMBOL)) {
            return Constants.NOTEQUAL_SYMBOL;
        } else if (strSymbol.equals(Constants.NOTEQUAL_SYMBOL)) {
            return Constants.EQUAL_SYMBOL;
        } else if (strSymbol.equals(Constants.ADD_SYMBOL)) {
            return Constants.SUBTRACT_SYMBOL;
        } else if (strSymbol.equals(Constants.SUBTRACT_SYMBOL)) {
            return Constants.ADD_SYMBOL;
        } else if (strSymbol.equals(Constants.MULTIPLY_SYMBOL)) {
            return Constants.DIVIDE_SYMBOL;
        } else if (strSymbol.equals(Constants.DIVIDE_SYMBOL)) {
            return Constants.MULTIPLY_SYMBOL;
        } else if (strSymbol.equals(Constants.GREATER_SYMBOL)) {
            return Constants.LESS_SYMBOL;
        } else if (strSymbol.equals(Constants.GREATEREQUAL_SYMBOL)) {
            return Constants.LESSEQUAL_SYMBOL;
        } else if (strSymbol.equals(Constants.LESS_SYMBOL)) {
            return Constants.GREATER_SYMBOL;
        } else if (strSymbol.equals(Constants.LESSEQUAL_SYMBOL)) {
            return Constants.GREATEREQUAL_SYMBOL;
        }
        return strSymbol;
    }

    /**
     * If the operate is equal, not equal, greater, less than, greater or equal, less than or equal.
     *
     * @param strSymbol
     * @return
     */
    public static boolean isPredicateOperatorCode(String strSymbol) {
        if (strSymbol.equals(Constants.EQUAL_SYMBOL)) {
            return true;
        } else if (strSymbol.equals(Constants.NOTEQUAL_SYMBOL)) {
            return true;
        } else if (strSymbol.equals(Constants.GREATER_SYMBOL)) {
            return true;
        } else if (strSymbol.equals(Constants.GREATEREQUAL_SYMBOL)) {
            return true;
        } else if (strSymbol.equals(Constants.LESS_SYMBOL)) {
            return true;
        } else if (strSymbol.equals(Constants.LESSEQUAL_SYMBOL)) {
            return true;
        } else {
            return false;
        }
    }

    public static String formatSlot(Object s) {
        if (s != null) {
            if (s instanceof Boolean) {
                return s.toString().toUpperCase();
            } else if (s instanceof String) {
                return "\"" + s.toString() + "\"";
            } else if (s.getClass() != null && s.getClass().isArray()) {
                StringBuilder buf = new StringBuilder();
                Object[] ary = (Object[]) s;
                for (int idx = 0; idx < ary.length; idx++) {
                    if (idx > 0) {
                        buf.append(" ");
                    }
                    buf.append(formatSlot(ary[idx]));
                }
                return buf.toString();
            } else {
                return s.toString();
            }
        } else {
            return Constants.NIL_SYMBOL;
        }
    }

    public static void main(String[] args) {
        /**
         * String[] left = {"one","two","three"}; String[] right = {"four","five"}; String[] m =
         * (String[])mergeFacts(left,right); for (int idx=0; idx < m.length; idx++){
         * System.out.println(m[idx]); }
         */
    }
}
