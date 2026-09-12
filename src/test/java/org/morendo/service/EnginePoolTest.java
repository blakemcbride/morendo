package org.morendo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import woolfel.examples.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** The engine pool of the service module under concurrent use. */
public class EnginePoolTest {

    private static final String CONFIG = "./samples/configuration/sample_config.json";

    static RuleService service() {
        RuleService service = RuleServiceImpl.createInstance(CONFIG);
        service.initialize();
        return service;
    }

    static RuleApplication app(RuleService service) {
        return (RuleApplication) service.getRuleApplications().get(0);
    }

    /** The facts an engine of the application holds before any request. */
    static int engineFacts(RuleService service, RuleApplication app) {
        EngineContext ctx = service.getEngine(app.getName(), app.getVersion());
        int count = ctx.getRuleEngine().getAllFacts().size();
        ctx.close();
        return count;
    }

    @Test
    public void manyThreadsShareThePool() throws Exception {
        RuleService service = service();
        try {
            RuleApplication app = app(service);
            EnginePool pool = service.getEnginePool(app.getName(), app.getVersion());
            assertNotNull(pool);
            int baseline = engineFacts(service, app);
            int threads = 8;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Integer>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(
                        executor.submit(
                                () -> {
                                    start.await();
                                    int used = 0;
                                    for (int i = 0; i < 20; i++) {
                                        EngineContext ctx =
                                                service.getEngine(app.getName(), app.getVersion());
                                        assertNotNull(ctx, "an engine within the timeout");
                                        Rete engine = ctx.getRuleEngine();
                                        assertEquals(
                                                baseline,
                                                engine.getAllFacts().size(),
                                                "a request starts with the initial data only");
                                        engine.assertObject(new Account(), null, false, true);
                                        ctx.executeRules();
                                        ctx.close();
                                        used++;
                                    }
                                    return used;
                                }));
            }
            start.countDown();
            for (Future<Integer> result : results) {
                assertEquals(20, result.get(60, TimeUnit.SECONDS));
            }
            executor.shutdown();
            assertTrue(pool.createdCount() <= app.getMaxPool(), "never past the maximum");
            assertEquals(pool.createdCount(), pool.idleCount(), "every engine came back");
        } finally {
            service.close();
        }
    }

    @Test
    public void checkOutWaitsForAnEngineToComeBack() throws Exception {
        RuleService service = RuleServiceImpl.createInstance(CONFIG);
        RuleApplication app = app(service);
        app.setMaxPool(1);
        app.setCheckoutTimeout(2000);
        service.initialize();
        try {
            EngineContext first = service.getEngine(app.getName(), app.getVersion());
            assertNotNull(first);
            Rete engine = first.getRuleEngine();
            Thread returner =
                    new Thread(
                            () -> {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                first.close();
                            });
            returner.start();
            long before = System.currentTimeMillis();
            EngineContext second = service.getEngine(app.getName(), app.getVersion());
            returner.join();
            assertNotNull(second, "the check-out waited for the engine");
            assertSame(engine, second.getRuleEngine());
            assertTrue(System.currentTimeMillis() - before >= 150, "it did wait");
            app.setCheckoutTimeout(100);
            assertNull(
                    service.getEngine(app.getName(), app.getVersion()),
                    "null when nothing comes back in time");
            second.close();
        } finally {
            service.close();
        }
    }

    @Test
    public void closeClearsTheEngineUnlessAsked() throws Exception {
        RuleService service = service();
        try {
            RuleApplication app = app(service);
            int baseline = engineFacts(service, app);
            EngineContext ctx = service.getEngine(app.getName(), app.getVersion());
            ctx.getRuleEngine().assertObject(new Account(), null, false, true);
            ctx.keepFacts();
            Rete engine = ctx.getRuleEngine();
            ctx.close();
            assertEquals(baseline + 1, engine.getAllFacts().size(), "kept on request");
            ctx = service.getEngine(app.getName(), app.getVersion());
            assertSame(engine, ctx.getRuleEngine());
            ctx.getRuleEngine().assertObject(new Account(), null, false, true);
            ctx.close();
            assertEquals(baseline + 1, engine.getAllFacts().size(), "the request's object went");
            ctx = service.getEngine(app.getName(), app.getVersion());
            ctx.close();
        } finally {
            service.close();
        }
    }
}
