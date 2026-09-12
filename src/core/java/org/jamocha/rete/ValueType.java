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
package org.jamocha.rete;

/**
 * The type a slot, parameter or function result carries. Replaces the integer type codes that used
 * to live in Constants; the names are the old constants without the _TYPE suffix.
 */
public enum ValueType {
    INT_PRIM,
    SHORT_PRIM,
    LONG_PRIM,
    FLOAT_PRIM,
    DOUBLE_PRIM,
    BYTE_PRIM,
    BOOLEAN_PRIM,
    CHAR_PRIM,
    OBJECT,
    ARRAY,
    STRING,
    RETURN_VOID,
    FACT,
    INTEGER_OBJECT,
    SHORT_OBJECT,
    LONG_OBJECT,
    FLOAT_OBJECT,
    DOUBLE_OBJECT,
    BYTE_OBJECT,
    BOOLEAN_OBJECT,
    BIG_INTEGER,
    BIG_DECIMAL,
    NUMERIC_INCLUSIVE,
    LIST,
    DATE,
    SLOT
}
