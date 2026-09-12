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
 * The comparison and arithmetic operators of constraints and predicate functions. Replaces the
 * integer operator codes that used to live in Constants.
 */
public enum Operator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    GREATER,
    LESS,
    GREATEREQUAL,
    LESSEQUAL,
    EQUAL,
    NOTEQUAL,
    NILL,
    NOTNILL,
    USERDEFINED
}
