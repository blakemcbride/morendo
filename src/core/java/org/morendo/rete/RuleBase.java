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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.morendo.parser.clips.CLIPSParser;
import org.morendo.parser.clips.ParseException;
import org.morendo.rete.util.IOUtilities;
import org.morendo.rule.Defrule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base set of templates, rules, functions and facts that any number of engines start from. The
 * steps are recorded once, CLIPS sources read into memory, and {@link #newEngine()} replays them
 * into a fresh engine, which from then on is independent of the base and of every other engine.
 *
 * <p>A rule base is safe to share between threads: each step added replaces the recorded list with
 * a new one, so {@code newEngine()} may be called from many threads at once, each building its own
 * engine. A Java object asserted by a {@link #configure} step is the same object in every engine
 * unless the step creates it, so base facts are best written in CLIPS.
 */
@NullMarked
public final class RuleBase {

    private volatile List<Consumer<Rete>> steps = List.of();

    /**
     * Records the CLIPS source at a location (a file path, URL or {@code classpath:} resource),
     * read now and kept in memory.
     *
     * @throws UncheckedIOException if the location cannot be read
     */
    public RuleBase load(String location) {
        try (InputStream in = IOUtilities.open(location)) {
            return add(clips(in.readAllBytes(), location));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + location, e);
        }
    }

    /**
     * Records the CLIPS source read from a stream, which the caller closes.
     *
     * @throws UncheckedIOException if the stream cannot be read
     */
    public RuleBase load(InputStream in) {
        try {
            return add(clips(in.readAllBytes(), "stream"));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read the rule base stream", e);
        }
    }

    /** Records CLIPS text, any number of constructs and calls. */
    public RuleBase eval(String clips) {
        return add(clips(clips.getBytes(StandardCharsets.UTF_8), "text"));
    }

    /** Records the declaration of a Java class as a template named after the class. */
    public RuleBase declareObject(Class<?> type) {
        return add(engine -> engine.declareObject(type));
    }

    /** Records the declaration of a Java class as a template of the given name. */
    public RuleBase declareObject(Class<?> type, @Nullable String templateName) {
        return add(engine -> engine.declareObject(type, templateName));
    }

    /**
     * Records any other setup, such as declaring Java functions or function groups. The step runs
     * once for every new engine, on the thread that calls {@link #newEngine()}.
     */
    public RuleBase configure(Consumer<Rete> step) {
        return add(step);
    }

    /**
     * Creates an engine and replays every recorded step into it, in the order they were added.
     *
     * @throws RuntimeException the first step's failure (a {@link IllegalArgumentException} for
     *     CLIPS that does not parse); the half-built engine is closed
     */
    public Rete newEngine() {
        List<Consumer<Rete>> recorded = steps;
        Rete engine = new Rete();
        try {
            for (Consumer<Rete> step : recorded) {
                step.accept(engine);
            }
        } catch (RuntimeException e) {
            engine.close();
            throw e;
        }
        return engine;
    }

    private synchronized RuleBase add(Consumer<Rete> step) {
        List<Consumer<Rete>> next = new ArrayList<>(steps);
        next.add(step);
        steps = List.copyOf(next);
        return this;
    }

    /**
     * A step that parses and runs CLIPS source as {@code batch} does, except that a parse error
     * throws instead of being printed.
     */
    private static Consumer<Rete> clips(byte[] source, String origin) {
        return engine -> {
            try {
                CLIPSParser parser = new CLIPSParser(engine, new ByteArrayInputStream(source));
                Object expr;
                while ((expr = parser.basicExpr()) != null) {
                    if (expr instanceof Defrule rl) {
                        engine.getRuleCompiler().addRule(rl);
                    } else if (expr instanceof Deftemplate dft) {
                        engine.getCurrentFocus()
                                .addTemplate(dft, engine, engine.getWorkingMemory());
                    } else if (expr instanceof Function fnc) {
                        fnc.executeFunction(engine, null);
                    }
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException(origin + ": " + e.getMessage(), e);
            }
        };
    }
}
