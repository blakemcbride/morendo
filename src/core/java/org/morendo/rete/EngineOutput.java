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
package org.morendo.rete;

import org.morendo.messagerouter.MessageEvent;
import org.morendo.messagerouter.MessageRouter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The engine's routers, in the CLIPS sense: where printed output goes and where input is read.
 *
 * <p>A message to {@code t} (or to any name that is not an open router) is posted to the message
 * router, which delivers it to the shell or GUI channel executing the command, and copied to every
 * console writer, such as a {@code (spool)} file or the golden tests' buffer. A message to a router
 * opened with {@code (open)} goes to that file alone. Input for {@code t} comes from the shell when
 * one is attached, else from standard input. Owned by Rete.
 */
public class EngineOutput {

    private final Rete engine;

    /** Writers that receive every message sent to the terminal. */
    private final Map<String, PrintWriter> consoles = new HashMap<>();

    /** Files opened for writing, by router name. */
    private final Map<String, PrintWriter> writers = new HashMap<>();

    /** Files opened for reading, by router name. */
    private final Map<String, BufferedReader> readers = new HashMap<>();

    private Supplier<String> inputSupplier = null;

    private BufferedReader standardInput = null;

    EngineOutput(Rete engine) {
        this.engine = engine;
    }

    /** Registers a console writer; a name is used to remove it again. */
    public void addPrintWriter(String name, Writer writer) {
        this.consoles.put(name, writer instanceof PrintWriter pw ? pw : new PrintWriter(writer));
    }

    /** Removes a console writer; the caller closes it. */
    public PrintWriter removePrintWriter(String name) {
        return this.consoles.remove(name);
    }

    /** Writes a message to the named router; "t" and unknown names go to the terminal. */
    public void write(String msg, String output) {
        PrintWriter file = this.writers.get(output);
        if (file != null) {
            file.write(msg);
            file.flush();
            return;
        }
        MessageRouter router = engine.getMessageRouter();
        router.postMessageEvent(
                new MessageEvent(
                        MessageEvent.Type.ENGINE,
                        msg,
                        "t".equals(output) ? router.getCurrentChannelId() : output));
        for (PrintWriter writer : this.consoles.values()) {
            writer.write(msg);
            writer.flush();
        }
    }

    /** Opens a file as a router: mode "r" to read, "w" to write, "a" to append. */
    public void open(String router, String file, String mode) throws IOException {
        Path path = Path.of(file);
        switch (mode) {
            case "r" -> {
                close(router);
                this.readers.put(router, Files.newBufferedReader(path, StandardCharsets.UTF_8));
            }
            case "w", "a" -> {
                close(router);
                Writer w =
                        "a".equals(mode)
                                ? Files.newBufferedWriter(
                                        path,
                                        StandardCharsets.UTF_8,
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.APPEND)
                                : Files.newBufferedWriter(path, StandardCharsets.UTF_8);
                this.writers.put(router, new PrintWriter(w));
            }
            default -> throw new IllegalArgumentException("mode must be r, w or a: " + mode);
        }
    }

    /** Registers a writer as a router; output sent to the name goes to it alone. */
    public void open(String router, Writer writer) {
        close(router);
        this.writers.put(router, writer instanceof PrintWriter pw ? pw : new PrintWriter(writer));
    }

    /** Closes a router, or every router when the name is null; true if something was closed. */
    public boolean close(String router) {
        boolean closed = false;
        for (String name :
                router == null
                        ? new ArrayList<>(this.writers.keySet())
                        : java.util.List.of(router)) {
            PrintWriter writer = this.writers.remove(name);
            if (writer != null) {
                writer.close();
                closed = true;
            }
        }
        for (String name :
                router == null
                        ? new ArrayList<>(this.readers.keySet())
                        : java.util.List.of(router)) {
            BufferedReader reader = this.readers.remove(name);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // nothing left to do with a reader that will not close
                }
                closed = true;
            }
        }
        return closed;
    }

    /** True when the name is an open router. */
    public boolean isOpen(String router) {
        return this.writers.containsKey(router) || this.readers.containsKey(router);
    }

    /** The shell installs this so that reading from "t" takes the next line the user types. */
    public void setInputSupplier(Supplier<String> supplier) {
        this.inputSupplier = supplier;
    }

    /** The next line from a reader router, or from the terminal for "t"; null at the end. */
    public String readLine(String router) {
        try {
            if ("t".equals(router) || !this.readers.containsKey(router)) {
                if (this.inputSupplier != null) {
                    return this.inputSupplier.get();
                }
                if (this.standardInput == null) {
                    this.standardInput =
                            new BufferedReader(
                                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
                }
                return this.standardInput.readLine();
            }
            return this.readers.get(router).readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
