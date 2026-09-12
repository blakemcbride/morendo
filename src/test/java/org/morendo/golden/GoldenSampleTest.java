package org.morendo.golden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.morendo.rete.Rete;

import woolfel.examples.model.Account;
import woolfel.examples.model.AccountHobby;
import woolfel.examples.model.Hobby;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Characterization ("golden master") tests for the rule engine.
 *
 * <p>Each scenario loads one or more CLIPS files into a fresh engine with rule watching turned on,
 * fires the agenda, and compares everything the engine printed plus a few working-memory and
 * network statistics against the checked-in file src/test/resources/golden/&lt;scenario&gt;.txt.
 *
 * <p>The tests must run from the repository root (all paths are relative to it).
 *
 * <p>To regenerate the golden files after an intentional behaviour change run "./bld golden-update"
 * (or "./bld golden-update name1,name2" for some scenarios), then review the diff before committing
 * it. Under the hood that sets -Dgolden.update=true and -Dgolden.only=... on the JUnit console
 * launcher.
 */
public class GoldenSampleTest {

    static final Path GOLDEN_DIR = Paths.get("src", "test", "resources", "golden");
    static final Path SCENARIO_DIR = Paths.get("src", "test", "resources", "scenarios");
    static final boolean UPDATE = Boolean.getBoolean("golden.update");

    /** Optional comma-separated list of scenario names to run (default: all). */
    static final String ONLY = System.getProperty("golden.only", "");

    /** Optional Java-side hooks for scenarios that need declared classes or asserted objects. */
    interface Hook {
        void apply(Rete engine) throws Exception;
    }

    static final class Scenario {
        final String name;
        final String[] files;
        final Hook before;
        final Hook after;

        Scenario(String name, String[] files, Hook before, Hook after) {
            this.name = name;
            this.files = files;
            this.before = before;
            this.after = after;
        }
    }

    @TestFactory
    Stream<DynamicTest> samples() {
        List<String> only =
                ONLY.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(ONLY.split(","));
        return scenarios().stream()
                .filter(scenario -> only.isEmpty() || only.contains(scenario.name))
                .map(scenario -> DynamicTest.dynamicTest(scenario.name, () -> check(scenario)));
    }

    static List<Scenario> scenarios() {
        List<Scenario> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            list.add(files("only_" + i, "samples/only/only_" + i + ".clp"));
        }
        list.add(files("exists_sample", "samples/exists/exists_sample.clp"));
        for (int i = 2; i <= 15; i++) {
            list.add(files("exists_sample" + i, "samples/exists/exists_sample" + i + ".clp"));
        }
        list.add(files("time_test_min_eq", "samples/time/test_min_eq.clp"));
        list.add(files("time_test_second_eq", "samples/time/test_second_eq.clp"));
        list.add(files("time_test_within_second", "samples/time/test_within_second.clp"));
        list.add(files("defquery_run", "samples/defquery/run.clp"));
        list.add(files("graphquery", SCENARIO_DIR.resolve("graphquery.clp").toString()));
        list.add(files("molap", SCENARIO_DIR.resolve("molap.clp").toString()));
        list.add(files("manners16", SCENARIO_DIR.resolve("manners16.clp").toString()));
        list.add(
                new Scenario(
                        "ruleset_sample1",
                        new String[] {"samples/ruleset/sample1.clp"},
                        new Hook() {
                            public void apply(Rete engine) {
                                engine.declareObject(Account.class);
                                engine.declareObject(AccountHobby.class);
                                engine.declareObject(Hobby.class);
                            }
                        },
                        new Hook() {
                            public void apply(Rete engine) throws Exception {
                                List<Object> objects = new ArrayList<>();
                                objects.add(account("1", "john", "doe", 35));
                                objects.add(account("2", "jane", "roe", 25));
                                objects.add(account("3", "sam", "poe", 31));
                                objects.add(accountHobby("1", "H1", 5));
                                objects.add(accountHobby("2", "H2", 3));
                                objects.add(accountHobby("3", "H1", 4));
                                objects.add(hobby("H1", "hiking"));
                                objects.add(hobby("H2", "chess"));
                                engine.assertObjects(objects);
                            }
                        }));
        return list;
    }

    static Scenario files(String name, String... files) {
        return new Scenario(name, files, null, null);
    }

    static Account account(String id, String first, String last, int age) {
        Account a = new Account();
        a.setAccountId(id);
        a.setFirst(first);
        a.setLast(last);
        a.setAge(age);
        return a;
    }

    static AccountHobby accountHobby(String accountId, String hobbyCode, int rating) {
        AccountHobby h = new AccountHobby();
        h.setAccountId(accountId);
        h.setHobbyCode(hobbyCode);
        h.setRating(rating);
        return h;
    }

    static Hobby hobby(String code, String name) {
        Hobby h = new Hobby();
        h.setHobbyCode(code);
        h.setName(name);
        return h;
    }

    /**
     * Runs one scenario and compares it with its golden file (or rewrites the file in update mode).
     */
    static void check(Scenario scenario) throws Exception {
        String actual = execute(scenario);
        Path golden = GOLDEN_DIR.resolve(scenario.name + ".txt");
        if (UPDATE) {
            Files.createDirectories(golden.getParent());
            Files.write(golden, actual.getBytes(StandardCharsets.UTF_8));
            return;
        }
        assertTrue(
                Files.exists(golden),
                "missing golden file " + golden + "; generate it with ./bld golden-update");
        String expected = new String(Files.readAllBytes(golden), StandardCharsets.UTF_8);
        assertEquals(
                expected,
                actual,
                "golden mismatch for "
                        + scenario.name
                        + " ("
                        + golden
                        + "); if the change is intended regenerate with ./bld golden-update "
                        + scenario.name);
    }

    static String execute(Scenario scenario) throws Exception {
        Rete engine = new Rete();
        StringWriter out = new StringWriter();
        engine.addPrintWriter("golden", new PrintWriter(out));
        engine.setWatch(Rete.Watch.RULES);
        try {
            if (scenario.before != null) {
                scenario.before.apply(engine);
            }
            for (String file : scenario.files) {
                engine.loadRuleset(file);
            }
            if (scenario.after != null) {
                scenario.after.apply(engine);
            }
            engine.fire();
            return render(engine, out.toString());
        } finally {
            engine.close();
        }
    }

    static String render(Rete engine, String output) {
        // Which rules fired, and in what order, is captured by the "==> fire:" lines that
        // (watch rules) writes to the output; Rete.getRulesFired() is not usable for this
        // because fire() records the rule only after clearing the activation.
        StringBuilder sb = new StringBuilder();
        sb.append("== stats ==\n");
        sb.append("templates: ").append(engine.getCurrentFocus().getTemplateCount()).append('\n');
        sb.append("rules: ").append(engine.getCurrentFocus().getRuleCount()).append('\n');
        sb.append("facts: ").append(engine.getAllFacts().size()).append('\n');
        sb.append("next-node-id: ").append(engine.peakNextNodeId()).append('\n');
        sb.append("== output ==\n");
        sb.append(normalize(output));
        if (sb.charAt(sb.length() - 1) != '\n') {
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Strips the parts of the output that legitimately differ between runs: platform line endings,
     * the activation aggregate time (built from fact timestamps) and printed dates (java.util.Date
     * and ISO-8601 Instant forms).
     */
    static String normalize(String output) {
        String s = output.replace("\r\n", "\n").replace('\r', '\n');
        s = s.replaceAll("AggrTime--?\\d+", "AggrTime-*");
        s =
                s.replaceAll(
                        "[A-Z][a-z]{2} [A-Z][a-z]{2} \\d{2} \\d{2}:\\d{2}:\\d{2} [A-Z]{2,5} \\d{4}",
                        "<DATE>");
        s = s.replaceAll("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?Z", "<DATE>");
        return s;
    }
}
