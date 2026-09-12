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

/**
 * @author Peter Lin
 */
public class Constants {

    public static final String PCS = "java.beans.PropertyChangeSupport";
    public static final String PCS_ADD = "addPropertyChangeListener";
    public static final String PCS_REMOVE = "removePropertyChangeListener";
    public static final String PROPERTYCHANGELISTENER = "java.beans.PropertyChangeListener";
    public static final String MAIN_MODULE = "MAIN";

    // value type codes: see ValueType

    // operator codes: see Operator

    /// --------- operators symbol ---------///
    public static final String ADD_SYMBOL = "+";
    public static final String SUBTRACT_SYMBOL = "-";
    public static final String MULTIPLY_SYMBOL = "*";
    public static final String DIVIDE_SYMBOL = "/";
    public static final String GREATER_SYMBOL = ">";
    public static final String LESS_SYMBOL = "<";
    public static final String GREATEREQUAL_SYMBOL = ">=";
    public static final String LESSEQUAL_SYMBOL = "<=";
    public static final String EQUAL_SYMBOL = "=";
    public static final String NOTEQUAL_SYMBOL = "!=";
    public static final String NIL_SYMBOL = "nil";

    /// --------- operators strings ---------///
    public static final String ADD_STRING = "add";
    public static final String SUBTRACT_STRING = "subtract";
    public static final String MULTIPLY_STRING = "multiply";
    public static final String DIVIDE_STRING = "divide";
    public static final String GREATER_STRING = "greater than";
    public static final String LESS_STRING = "less than";
    public static final String GREATEREQUAL_STRING = "greater than or equal to";
    public static final String LESSEQUAL_STRING = "less than or equal to";
    public static final String EQUAL_STRING = "equal to";
    public static final String NOTEQUAL_STRING = "not equal to";
    public static final String NILL_STRING = "is null";

    /// --------- native types for the rule engine ---------///

    public static final int ACTION_ASSERT = 1000;
    public static final int ACTION_RETRACT = 1001;
    public static final int ACTION_MODIFY = 1002;

    /// ----------- constants for chaining direction -------///
    public static final int FORWARD_CHAINING = 10000;
    public static final int BACKWARD_CHAINING = 10001;
    public static final int BIDIRECTIONAL_CHAINING = 10002;
    public static final int LAZY_CHAINING = 10003;

    public static final String LINEBREAK = System.getProperty("line.separator");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String CRLF = "crlf";
    public static final String SHELL_PROMPT = "Morendo> ";
    public static final String DEFAULT_OUTPUT = "t";
    public static final String VERSION =
            "2.0.0"; // the single source of the version; builder/Tasks.java reads it
    public static final String INITIAL_FACT = "_initialFact";
    public static final String COUNT_FACT = "_countFact";
    public static final String COUNT_SLOT = "count";
    public static final String COUNT_VALUE = "value";
    public static final String PROJECT_MESSAGE =
            "Copyright Jamocha Project http://sourceforge.net/projects/jamocha";
    public static final String SHELL_MESSAGE = "Morendo Version " + VERSION;

    /// --------------- working directory ----------------- ///
    public static final String WORKING_DIRECTORY = "./working_directory";
}
