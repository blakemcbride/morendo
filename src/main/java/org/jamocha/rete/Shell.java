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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jamocha.messagerouter.MessageEvent;
import org.jamocha.messagerouter.MessageRouter;
import org.jamocha.messagerouter.StringChannel;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * The interactive CLIPS shell started by "morendo -shell".
 *
 * Input is read with JLine (line editing, history in ~/.morendo_history; a dumb terminal
 * when the input is a pipe) and collected until the parentheses balance, so a construct
 * such as a defrule can be typed over several lines. Each complete expression goes to the
 * engine through a StringChannel and everything the engine sends back is printed.
 * End of input (Ctrl-D, or the end of piped input) ends the process like (exit) does.
 */
public class Shell {

	public static final String CHANNELNAME = "Shell";

	private static final String CONTINUATION_PROMPT = "... ";

	private final StringChannel channel;

	public Shell(Rete engine) {
		MessageRouter router = engine.getMessageRouter();
		channel = router.openChannel(CHANNELNAME);
		router.setCurrentChannelId(channel.getChannelId());
	}

	public void run() {
		System.out.println(Constants.PROJECT_MESSAGE);
		System.out.println(Constants.SHELL_MESSAGE);
		try (Terminal terminal = TerminalBuilder.builder().system(true).dumb(true).build()) {
			LineReader reader = LineReaderBuilder.builder().terminal(terminal)
					.variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".morendo_history"))
					.build();
			StringBuilder pending = new StringBuilder();
			while (true) {
				String line;
				try {
					line = reader.readLine(pending.length() == 0 ? Constants.SHELL_PROMPT : CONTINUATION_PROMPT);
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
				}
			}
		} catch (IOException e) {
			System.err.println("cannot open the terminal: " + e.getMessage());
		}
		// The engine's message router runs a non-daemon thread, so end the process explicitly.
		System.exit(0);
	}

	/** Parses and executes the text, waiting for each expression's result, and prints the events. */
	private void execute(String text) {
		channel.executeCommand(text, true);
		List<MessageEvent> events = new ArrayList<MessageEvent>();
		channel.fillEventList(events);
		for (MessageEvent event : events) {
			print(event);
		}
		System.out.flush();
	}

	private void print(MessageEvent event) {
		if (event.getType() == MessageEvent.COMMAND) {
			return;
		}
		Object message = event.getMessage();
		if (event.getType() == MessageEvent.ERROR && message instanceof Exception) {
			System.out.println(stackTrace((Exception) message).trim());
		}
		if (message instanceof DefaultReturnVector) {
			DefaultReturnVector rv = (DefaultReturnVector) message;
			if (rv.getItems().size() > 0) {
				ReturnValue rval = (ReturnValue) rv.getItems().firstElement();
				if (rval.getValueType() == Constants.ARRAY_TYPE || rval.getValueType() == Constants.LIST_TYPE) {
					System.out.println(Arrays.toString((Object[]) rval.getValue()));
				} else {
					System.out.print(message.toString());
				}
			}
		} else if (message != null && !message.toString().isEmpty()) {
			System.out.print(message.toString());
		}
	}

	/**
	 * True when the text holds at least one complete expression: every "(" is closed,
	 * ignoring parentheses inside double-quoted strings and after a ";" comment marker.
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
			if (c == ';') {
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
