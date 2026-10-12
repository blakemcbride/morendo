package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Fact;
import org.morendo.rete.Rete;
import org.morendo.rete.RuleBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Engines started from one {@link RuleBase}, on many threads at once. */
public class RuleBaseTest {

    private static final String BASE =
            """
            (deftemplate item (slot n))
            (deftemplate result (slot n))
            (defrule double (item (n ?n)) => (assert (result (n (* 2 ?n)))))
            (deffacts base (item (n 1)) (item (n 2)))
            (reset)
            """;

    /** The values of slot n of the facts of a template, sorted. */
    static List<Long> values(Rete engine, String template) {
        List<Long> out = new ArrayList<>();
        for (Fact f : engine.getAllFacts()) {
            if (f.getDeftemplate().getName().equals(template)) {
                out.add(((Number) f.getSlotValue(0)).longValue());
            }
        }
        out.sort(null);
        return out;
    }

    @Test
    public void everyEngineStartsFromTheBase() {
        RuleBase base = new RuleBase().eval(BASE);
        Rete a = base.newEngine();
        Rete b = base.newEngine();
        try {
            assertNotSame(a, b);
            assertEquals(List.of(1L, 2L), values(a, "item"));
            assertEquals(2, a.fire());
            assertEquals(List.of(2L, 4L), values(a, "result"));
            assertEquals(List.of(), values(b, "result"), "firing one engine leaves the other");
            a.loadRuleset(
                    new java.io.ByteArrayInputStream(
                            "(defrule extra (item (n 1)) => (assert (result (n 0))))".getBytes()));
            assertTrue(a.getCurrentFocus().getRuleCount() > b.getCurrentFocus().getRuleCount());
            Rete c = base.newEngine();
            assertEquals(1, c.getCurrentFocus().getRuleCount(), "a later engine has the base only");
            c.close();
        } finally {
            a.close();
            b.close();
        }
    }

    @Test
    public void manyThreadsEachBuildTheirOwnEngine() throws Exception {
        RuleBase base = new RuleBase().eval(BASE);
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<List<Long>>> results = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            long n = 10 + t;
            results.add(
                    executor.submit(
                            () -> {
                                start.await();
                                List<Long> last = null;
                                for (int i = 0; i < 25; i++) {
                                    Rete engine = base.newEngine();
                                    engine.loadRuleset(
                                            new java.io.ByteArrayInputStream(
                                                    ("(assert (item (n " + n + ")))").getBytes()));
                                    engine.fire();
                                    last = values(engine, "result");
                                    engine.close();
                                }
                                return last;
                            }));
        }
        start.countDown();
        for (int t = 0; t < threads; t++) {
            assertEquals(List.of(2L, 4L, 2L * (10 + t)), results.get(t).get(30, TimeUnit.SECONDS));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    public void stepsRunInOrder() {
        List<String> seen = new ArrayList<>();
        RuleBase base =
                new RuleBase()
                        .configure(engine -> seen.add("first"))
                        .eval("(deftemplate item (slot n))")
                        .configure(
                                engine ->
                                        seen.add(engine.findTemplate("item") == null ? "" : "two"))
                        .declareObject(woolfel.examples.model.Account.class);
        Rete engine = base.newEngine();
        try {
            assertEquals(List.of("first", "two"), seen);
            assertTrue(engine.findTemplate("woolfel.examples.model.Account") != null);
        } finally {
            engine.close();
        }
    }

    @Test
    public void aParseErrorThrows() {
        RuleBase base = new RuleBase().eval("(deftemplate item (slot n)) (defrule broken (item");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, base::newEngine);
        assertTrue(e.getMessage().startsWith("text: "), e.getMessage());
    }

    @Test
    public void aMissingFileThrowsWhenRecorded() {
        assertThrows(
                java.io.UncheckedIOException.class, () -> new RuleBase().load("no/such/file.clp"));
    }
}
