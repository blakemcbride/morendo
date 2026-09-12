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
package org.morendo.shell;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.morendo.messagerouter.MessageEvent;
import org.morendo.messagerouter.MessageRouter;
import org.morendo.messagerouter.StringChannel;
import org.morendo.rete.Constants;
import org.morendo.rete.DefaultReturnVector;
import org.morendo.rete.Rete;
import org.morendo.rete.ReturnValue;
import org.morendo.rete.ValueType;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The interactive CLIPS shell started by "morendo -shell".
 *
 * <p>Input is read with JLine (line editing, history in ~/.morendo_history; a dumb terminal when
 * the input is a pipe) and collected until the parentheses balance, so a construct such as a
 * defrule can be typed over several lines. Each complete expression goes to the engine through a
 * StringChannel and everything the engine sends back is printed. The shell returns when the input
 * ends (Ctrl-D, or the end of piped input) or when the engine has been closed, which is what (exit)
 * does.
 */
public class Shell {

    public static final String CHANNELNAME = "Shell";

    private static final String CONTINUATION_PROMPT = "... ";

    private final Rete engine;

    private final StringChannel channel;

    public Shell(Rete engine) {
        this.engine = engine;
        MessageRouter router = engine.getMessageRouter();
        channel = router.openChannel(CHANNELNAME);
        router.setCurrentChannelId(channel.getChannelId());
    }

    public void run() {
        System.out.println(Constants.PROJECT_MESSAGE);
        System.out.println(Constants.SHELL_MESSAGE);
        try (Terminal terminal = TerminalBuilder.builder().system(true).dumb(true).build()) {
            LineReader reader =
                    LineReaderBuilder.builder()
                            .terminal(terminal)
                            .variable(
                                    LineReader.HISTORY_FILE,
                                    Paths.get(System.getProperty("user.home"), ".morendo_history"))
                            .build();
            // (readline t) and (read t) inside a command take the next line typed here
            engine.setInputSupplier(
                    () -> {
                        try {
                            return reader.readLine("");
                        } catch (UserInterruptException | EndOfFileException e) {
                            return null;
                        }
                    });
            StringBuilder pending = new StringBuilder();
            while (true) {
                String line;
                try {
                    line =
                            reader.readLine(
                                    pending.length() == 0
                                            ? Constants.SHELL_PROMPT
                                            : CONTINUATION_PROMPT);
                } catch (UserInterruptException e) {
                    // Ctrl-C: drop whatever was typed so far
                    pending.setLength(0);
                    continue;
                } catch (EndOfFileException e) {
                    break;
                }
                pending.append(line).append('\n');
                if (isComplete(pending)) {
                    execute(pending.toString());
                    pending.setLength(0);
                    if (engine.isClosed()) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("cannot open the terminal: " + e.getMessage());
        }
    }

    /**
     * Parses and executes the text, waiting for each expression's result, and prints the events.
     */
    private void execute(String text) {
        channel.executeCommand(text, true);
        List<MessageEvent> events = new ArrayList<>();
        channel.fillEventList(events);
        for (MessageEvent event : events) {
            print(event);
        }
        System.out.flush();
    }

    private void print(MessageEvent event) {
        if (event.getType() == MessageEvent.Type.COMMAND) {
            return;
        }
        Object message = event.getMessage();
        if (event.getType() == MessageEvent.Type.ERROR && message instanceof Exception) {
            System.out.println(stackTrace((Exception) message).trim());
        }
        if (message instanceof DefaultReturnVector rv) {
            if (rv.getItems().size() > 0) {
                ReturnValue rval = rv.getItems().get(0);
                if (rval.getValueType() == ValueType.ARRAY
                        || rval.getValueType() == ValueType.LIST) {
                    System.out.println(Arrays.deepToString((Object[]) rval.getValue()));
                } else if (rval.getValue() instanceof java.util.List<?> list) {
                    // query results: a list of fact tuples
                    System.out.println(formatTuples(list));
                } else {
                    System.out.print(message.toString());
                }
            }
        } else if (message != null && !message.toString().isEmpty()) {
            System.out.print(message.toString());
        }
    }

    /** Prints a list of fact tuples one tuple per line, facts in their (facts) form. */
    private static String formatTuples(java.util.List<?> tuples) {
        StringBuilder buf = new StringBuilder();
        for (Object tuple : tuples) {
            if (buf.length() > 0) {
                buf.append(System.lineSeparator());
            }
            if (tuple instanceof Object[] facts) {
                for (int i = 0; i < facts.length; i++) {
                    if (i > 0) {
                        buf.append(' ');
                    }
                    buf.append(
                            facts[i] instanceof org.morendo.rete.Fact fact
                                    ? fact.toFactString()
                                    : String.valueOf(facts[i]));
                }
            } else if (tuple instanceof org.morendo.rete.Fact fact) {
                buf.append(fact.toFactString());
            } else {
                buf.append(tuple);
            }
        }
        return buf.toString();
    }

    /**
     * True when the text holds at least one complete expression: every "(" is closed, ignoring
     * parentheses inside double-quoted strings and after a ";;" comment marker (the grammar's
     * comment token; a single ";" is an ordinary token).
     */
    static boolean isComplete(CharSequence text) {
        int depth = 0;
        boolean inString = false;
        boolean sawSomething = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == ';' && i + 1 < text.length() && text.charAt(i + 1) == ';') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                sawSomething = true;
            } else if (c == '(') {
                depth++;
                sawSomething = true;
            } else if (c == ')') {
                depth--;
            } else if (!Character.isWhitespace(c)) {
                sawSomething = true;
            }
        }
        return sawSomething && !inString && depth <= 0;
    }

    private static String stackTrace(Exception exception) {
        StringBuilder res = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            res.append(element).append(System.lineSeparator());
        }
        return res.toString();
    }
}
