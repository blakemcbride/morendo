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

import org.jamocha.messagerouter.MessageEvent;
import org.jamocha.messagerouter.MessageRouter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Where the engine's printed output goes: every message is posted to the message router (which
 * delivers it to the shell or GUI channel that is executing a command) and to every registered
 * writer, such as a (spool) file. Owned by Rete.
 */
public class EngineOutput {

    private final Rete engine;

    private final Map<String, PrintWriter> writers = new HashMap<>();

    EngineOutput(Rete engine) {
        this.engine = engine;
    }

    /** Registers a writer that receives every message; a name is used to remove it again. */
    public void addPrintWriter(String name, Writer writer) {
        this.writers.put(name, writer instanceof PrintWriter pw ? pw : new PrintWriter(writer));
    }

    /** Removes a writer; the caller closes it. */
    public PrintWriter removePrintWriter(String name) {
        return this.writers.remove(name);
    }

    /** Writes a message to the named output ("t" is the current channel) and to the writers. */
    public void write(String msg, String output) {
        MessageRouter router = engine.getMessageRouter();
        router.postMessageEvent(
                new MessageEvent(
                        MessageEvent.Type.ENGINE,
                        msg,
                        "t".equals(output) ? router.getCurrentChannelId() : output));
        for (PrintWriter writer : this.writers.values()) {
            writer.write(msg);
            writer.flush();
        }
    }
}
