package builder;

import static builder.BuildUtils.*;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build tasks for Morendo, run through ./bld (bld.cmd on Windows). The generic helpers live in
 * builder/BuildUtils.java and are not meant to be edited; this file is the whole build.
 *
 * <p>Layout: one source root per module (see MODULES), src/<module>/java with resources in
 * src/<module>/resources, compiled to target/<module>/classes and packaged as
 * target/morendo-<module>-<version>.jar. Tests are in src/test/java (goldens and scenario scripts
 * in src/test/resources) and compile against every module. The CLIPS grammar is
 * src/core/javacc/clips.jj; the parser is generated into src/core/java/org/jamocha/parser/clips
 * (git-ignored there). libs/ holds the downloaded jars (libs/tools the build-time tools).
 *
 * <p>The version number lives in one place: Constants.VERSION in the core module.
 */
public class Tasks {

    static final String LIBS = "libs";
    static final String TOOLS = LIBS + "/tools";
    static final String BUILDDIR = "target";
    static final String TEST_CLASSES = BUILDDIR + "/test-classes";
    static final String TEST_SRC = "src/test/java";
    static final String GRAMMAR = "src/core/javacc/clips.jj";
    static final String PARSER_DIR = "src/core/java/org/jamocha/parser/clips";
    static final String GENERATED_PARSER_FILES =
            "CLIPSParser.*\\.java|ParseException\\.java|SimpleCharStream\\.java|Token\\.java|TokenMgrError\\.java";
    static final String CONSTANTS = "src/core/java/org/jamocha/rete/Constants.java";
    static final String MAIN_CLASS = "org.jamocha.Morendo";
    static final String GOLDEN_TESTS = "org.jamocha.golden.GoldenSampleTest";
    static final String MAVEN = "https://repo1.maven.org/maven2/";
    static final String JAVACC = "javacc-7.0.13.jar";
    static final String JUNIT = "junit-platform-console-standalone-6.1.3.jar";
    static final String FORMATTER = "google-java-format-1.36.1-all-deps.jar";
    static final String SPOTBUGS_VERSION = "4.10.4";
    static final String SPOTBUGS_HOME = TOOLS + "/spotbugs-" + SPOTBUGS_VERSION;

    /** The third-party jars, by short name; modules pick the ones they may use. */
    static final String[][] CATALOG = {
        {"log4j-api", MAVEN + "org/apache/logging/log4j/log4j-api/2.26.1/log4j-api-2.26.1.jar"},
        {"log4j-core", MAVEN + "org/apache/logging/log4j/log4j-core/2.26.1/log4j-core-2.26.1.jar"},
        {
            "jackson-core",
            MAVEN + "com/fasterxml/jackson/core/jackson-core/2.22.2/jackson-core-2.22.2.jar"
        },
        {
            "jackson-databind",
            MAVEN + "com/fasterxml/jackson/core/jackson-databind/2.22.2/jackson-databind-2.22.2.jar"
        },
        {
            "jackson-annotations",
            MAVEN
                    + "com/fasterxml/jackson/core/jackson-annotations/2.22/jackson-annotations-2.22.jar"
        },
        {"jakarta.jms-api", MAVEN + "jakarta/jms/jakarta.jms-api/3.1.0/jakarta.jms-api-3.1.0.jar"},
        {
            "jakarta.servlet-api",
            MAVEN + "jakarta/servlet/jakarta.servlet-api/6.1.0/jakarta.servlet-api-6.1.0.jar"
        },
        {"jline", MAVEN + "org/jline/jline/4.4.3/jline-4.4.3.jar"},
        // tests only; dist() leaves it out of the distribution
        {"junit", MAVEN + "org/junit/platform/junit-platform-console-standalone/6.1.3/" + JUNIT},
    };

    /**
     * A module: its source root is src/<name>/java (resources in src/<name>/resources), it is
     * compiled against the class directories of the modules it depends on and only the third-party
     * jars it names, so the compiler enforces the dependency rules.
     */
    record Module(String name, String[] dependsOn, String[] jars) {
        String src() {
            return "src/" + name + "/java";
        }

        String resources() {
            return "src/" + name + "/resources";
        }

        String classes() {
            return BUILDDIR + "/" + name + "/classes";
        }
    }

    static final Module[] MODULES = {
        new Module("core", new String[0], new String[] {"log4j-api", "log4j-core"}),
        new Module("examples", new String[] {"core"}, new String[] {"log4j-api"}),
        new Module(
                "messaging", new String[] {"core"}, new String[] {"log4j-api", "jakarta.jms-api"}),
        new Module("gui", new String[] {"core"}, new String[] {"log4j-api"}),
        new Module(
                "service",
                new String[] {"core"},
                new String[] {
                    "log4j-api",
                    "jackson-core",
                    "jackson-databind",
                    "jackson-annotations",
                    "jakarta.servlet-api"
                }),
        new Module("shell", new String[] {"core", "gui"}, new String[] {"log4j-api", "jline"}),
    };

    static final ForeignDependencies foreignLibs =
            jars(java.util.Arrays.stream(CATALOG).map(e -> e[0]).toArray(String[]::new));
    static final ForeignDependencies toolLibs = buildToolDependencies();
    private static String[] args;

    public static void main(String[] args) throws Exception {
        Tasks.args = args;
        BuildUtils.build(args, Tasks.class, LIBS);
    }

    public static void listTasks() {
        println("");
        println(
                "build                    download dependencies, generate the parser, compile every"
                        + " module");
        println(
                "test [class]             build and run all tests under src/test/java, or one test"
                        + " class");
        println(
                "golden-update [names]    regenerate the golden files (all, or a comma-separated"
                        + " list of scenarios)");
        println(
                "run <class> [argument]... build and run a class, e.g. bld run org.jamocha.Morendo"
                        + " -gui");
        println(
                "                         (for the interactive shell use ./morendo -shell"
                        + " instead)");
        println(
                "parser                   regenerate the CLIPS parser from src/main/javacc/clips.jj"
                        + " if it changed");
        println(
                "jar                      build target/morendo-<module>-<version>.jar for every"
                        + " module");
        println(
                "dist                     build target/morendo-<version>.zip (module jars, libs,"
                        + " launcher, samples)");
        println("javadoc                  build target/javadoc");
        println(
                "lint                     compile all module sources with -Xlint:all; summary on"
                        + " the console, details in target/lint.txt");
        println(
                "format                   reformat every Java source with google-java-format (AOSP"
                        + " style)");
        println("format-check             fail if any Java source is not formatted (used by CI)");
        println(
                "spotbugs                 run SpotBugs over the module classes; summary on the"
                        + " console, details in target/spotbugs.txt");
        println("libs                     download the jar files into libs/");
        println("");
        println("clean                    remove target/ and the generated parser sources");
        println("realclean                + remove downloaded jar files");
        println("ideclean                 + IDE files");
        println("");
    }

    // ---------------------------------------------------------------- tasks

    public static void libs() {
        guard(
                () -> {
                    downloadAll(foreignLibs);
                    downloadAll(toolLibs);
                });
    }

    public static void parser() {
        guard(Tasks::doParser);
    }

    public static void build() {
        guard(Tasks::doBuild);
    }

    public static void buildTests() {
        guard(Tasks::doBuildTests);
    }

    public static void test() {
        guard(
                () -> {
                    doBuildTests();
                    String[] targs = taskArgs();
                    String selection =
                            targs.length > 0
                                    ? "--select-class " + targs[0]
                                    : "--scan-class-path " + TEST_CLASSES;
                    runWait(true, junitCommand("") + selection);
                });
    }

    public static void goldenUpdate() {
        guard(
                () -> {
                    doBuildTests();
                    String[] targs = taskArgs();
                    String only = targs.length > 0 ? " -Dgolden.only=" + targs[0] : "";
                    runWait(
                            true,
                            junitCommand("-Dgolden.update=true" + only)
                                    + "--select-class "
                                    + GOLDEN_TESTS);
                    println(
                            "golden files written to src/test/resources/golden; review the diff"
                                    + " before committing");
                });
    }

    public static void run() {
        guard(
                () -> {
                    doBuild();
                    LocalDependencies deps = new LocalDependencies();
                    for (Module module : MODULES)
                        deps.add(BUILDDIR + "/" + module.name() + "/", "classes");
                    runJava(BUILDDIR + "/shell/classes", deps, foreignLibs);
                });
    }

    public static void jar() {
        guard(Tasks::doJar);
    }

    public static void dist() {
        guard(Tasks::doDist);
    }

    public static void javadoc() {
        guard(Tasks::doJavadoc);
    }

    public static void lint() {
        guard(Tasks::doLint);
    }

    public static void format() {
        guard(() -> runFormatter(false));
    }

    public static void formatCheck() {
        guard(() -> runFormatter(true));
    }

    public static void spotbugs() {
        guard(Tasks::doSpotbugs);
    }

    public static void clean() {
        rmTree(BUILDDIR);
        rmRegex(PARSER_DIR, GENERATED_PARSER_FILES);
    }

    public static void realclean() {
        clean();
        rmTree(LIBS);
        rmRegex("builder", ".*\\.class");
    }

    public static void ideclean() {
        realclean();
        rmTree(".settings");
        rmTree(".vscode");
        rmTree(".idea");
        rmTree("out");
        rmRegex(".", ".*\\.iml");
        rmTree("nbproject");
        rmTree("build");
    }

    // ---------------------------------------------------------------- task bodies

    private static void doParser() {
        downloadAll(toolLibs);
        File grammar = new File(GRAMMAR);
        File generated = new File(PARSER_DIR, "CLIPSParser.java");
        if (!generated.exists() || generated.lastModified() < grammar.lastModified()) {
            println("generating the CLIPS parser from " + GRAMMAR);
            runWait(
                    true,
                    "java -cp "
                            + TOOLS
                            + "/"
                            + JAVACC
                            + " javacc -OUTPUT_DIRECTORY="
                            + PARSER_DIR
                            + " "
                            + GRAMMAR);
        }
    }

    /** Compiles every module in dependency order, each against its own dependencies only. */
    private static void doBuild() {
        downloadAll(foreignLibs);
        doParser();
        for (Module module : MODULES) {
            mkdir(module.classes());
            LocalDependencies deps = new LocalDependencies();
            for (String dep : module.dependsOn()) deps.add(BUILDDIR + "/" + dep + "/", "classes");
            buildJava(module.src(), module.classes(), deps, jars(module.jars()), null);
            if (new File(module.resources()).isDirectory())
                copyTree(module.resources(), module.classes());
        }
    }

    private static void doBuildTests() {
        doBuild();
        mkdir(TEST_CLASSES);
        LocalDependencies deps = new LocalDependencies();
        for (Module module : MODULES) deps.add(BUILDDIR + "/" + module.name() + "/", "classes");
        buildJava(TEST_SRC, TEST_CLASSES, deps, foreignLibs, null);
    }

    /**
     * BuildUtils.buildJavadoc cannot pass options, and the 2000s-era comments in this code base
     * fail doclint's HTML checks, so javadoc is invoked directly with doclint off.
     */
    private static void doJavadoc() {
        downloadAll(foreignLibs);
        doParser();
        String dest = BUILDDIR + "/javadoc";
        try {
            java.util.List<java.nio.file.Path> sources = moduleSources();
            long newest = 0;
            for (java.nio.file.Path f : sources)
                newest = Math.max(newest, f.toFile().lastModified());
            File index = new File(dest, "index.html");
            if (index.exists() && index.lastModified() >= newest) return;
            mkdir(dest);
            File argsFile = File.createTempFile("javadoc-", ".args");
            StringBuilder sb = new StringBuilder();
            for (java.nio.file.Path f : sources) sb.append(f).append('\n');
            Files.write(argsFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            runWait(
                    true,
                    "javadoc -Xdoclint:none -quiet -d "
                            + dest
                            + " -cp "
                            + LIBS
                            + "/* @"
                            + argsFile);
            rm(argsFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException("javadoc: " + e.getMessage());
        }
    }

    /**
     * Compiles all module sources with every javac lint category enabled, writes the full output to
     * target/lint.txt and prints a count per category. A separate compilation (into target/lint) so
     * the normal incremental build is not affected.
     */
    private static void doLint() {
        downloadAll(foreignLibs);
        doParser();
        String dest = BUILDDIR + "/lint";
        rmTree(dest);
        mkdir(dest);
        try {
            java.util.List<String> sources =
                    moduleSources().stream()
                            .map(java.nio.file.Path::toString)
                            .collect(java.util.stream.Collectors.toList());
            File argsFile = File.createTempFile("lint-", ".args");
            Files.write(
                    argsFile.toPath(),
                    (String.join("\n", sources) + "\n").getBytes(StandardCharsets.UTF_8));
            StringBuilder cp = new StringBuilder();
            for (int i = 0; i < foreignLibs.size(); i++)
                cp.append(i == 0 ? "" : File.pathSeparator).append(foreignLibs.get(i));
            java.util.List<String> cmd =
                    Arrays.asList(
                            "javac",
                            "-Xlint:all",
                            "-Xmaxwarns",
                            "100000",
                            "-proc:none",
                            "-encoding",
                            "UTF-8",
                            "-d",
                            dest,
                            "-cp",
                            cp.toString(),
                            "@" + argsFile);
            Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String output =
                    new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int status = proc.waitFor();
            rm(argsFile.getPath());
            Files.write(Paths.get(BUILDDIR, "lint.txt"), output.getBytes(StandardCharsets.UTF_8));
            java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
            // count warnings per category, leaving out the generated parser sources (not ours to
            // fix)
            Matcher m =
                    Pattern.compile("(?m)^(\\S+\\.java):\\d+: warning: \\[([a-z-]+)\\]")
                            .matcher(output);
            while (m.find())
                if (!isGeneratedParserFile(Paths.get(m.group(1))))
                    counts.merge(m.group(2), 1, Integer::sum);
            int total = 0;
            for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
                println(String.format("%6d  %s", e.getValue(), e.getKey()));
                total += e.getValue();
            }
            println(String.format("%6d  total warnings (details in %s/lint.txt)", total, BUILDDIR));
            if (status != 0)
                throw new RuntimeException("javac reported errors; see " + BUILDDIR + "/lint.txt");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("lint: " + e.getMessage());
        }
    }

    /**
     * google-java-format in AOSP style (4-space indentation) over every hand-written Java source:
     * the modules, the tests and this build file. With check=true nothing is written and the task
     * fails if a file would change.
     */
    private static void runFormatter(boolean check) {
        downloadAll(toolLibs);
        try {
            java.util.List<String> cmd =
                    new java.util.ArrayList<>(
                            Arrays.asList(
                                    "java",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                                    "--add-exports",
                                    "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                                    "-jar",
                                    TOOLS + "/" + FORMATTER,
                                    "--aosp"));
            cmd.addAll(
                    check
                            ? Arrays.asList("--dry-run", "--set-exit-if-changed")
                            : Arrays.asList("--replace"));
            java.util.List<java.nio.file.Path> sources = moduleSources();
            try (java.util.stream.Stream<java.nio.file.Path> walk =
                    Files.walk(Paths.get(TEST_SRC))) {
                walk.filter(f -> f.toString().endsWith(".java")).forEach(sources::add);
            }
            sources.add(Paths.get("builder/Tasks.java"));
            for (java.nio.file.Path f : sources)
                if (!isGeneratedParserFile(f)) cmd.add(f.toString());
            Process proc = new ProcessBuilder(cmd).inheritIO().start();
            int status = proc.waitFor();
            if (status != 0)
                throw new RuntimeException(
                        check
                                ? "some sources are not formatted; run ./bld format"
                                : "google-java-format failed");
            println(check ? "all sources are formatted" : "sources formatted");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("format: " + e.getMessage());
        }
    }

    /**
     * SpotBugs over the compiled modules (examples left out). The full report goes to
     * target/spotbugs.txt; the console gets a count per priority. High-priority findings fail the
     * task, which is what CI checks.
     */
    private static void doSpotbugs() {
        doBuild();
        downloadAll(toolLibs);
        if (!new File(SPOTBUGS_HOME, "lib").isDirectory()) unJar(TOOLS, SPOTBUGS_HOME + ".zip");
        try {
            StringBuilder aux = new StringBuilder();
            for (int i = 0; i < foreignLibs.size(); i++)
                aux.append(i == 0 ? "" : File.pathSeparator).append(foreignLibs.get(i));
            java.util.List<String> cmd =
                    new java.util.ArrayList<>(
                            Arrays.asList(
                                    "java",
                                    "-cp",
                                    SPOTBUGS_HOME + "/lib/*",
                                    "edu.umd.cs.findbugs.LaunchAppMain",
                                    "-textui",
                                    "-effort:default",
                                    "-low",
                                    "-sortByClass",
                                    "-exclude",
                                    "spotbugs-exclude.xml",
                                    "-auxclasspath",
                                    aux.toString(),
                                    "-output",
                                    BUILDDIR + "/spotbugs.txt"));
            for (Module module : MODULES)
                if (!module.name().equals("examples")) cmd.add(module.classes());
            Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String console =
                    new String(proc.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int status = proc.waitFor();
            if (status != 0) throw new RuntimeException("SpotBugs failed:\n" + console);
            int high = 0, medium = 0, low = 0;
            for (String line : Files.readAllLines(Paths.get(BUILDDIR, "spotbugs.txt"))) {
                if (line.startsWith("H ")) high++;
                else if (line.startsWith("M ")) medium++;
                else if (line.startsWith("L ")) low++;
            }
            println(
                    String.format(
                            "SpotBugs: %d high, %d medium, %d low priority findings (details in"
                                    + " %s/spotbugs.txt)",
                            high, medium, low, BUILDDIR));
            if (high > 0) throw new RuntimeException("SpotBugs reported high-priority findings");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("spotbugs: " + e.getMessage());
        }
    }

    /** Every .java file of every module, sorted. */
    private static java.util.List<java.nio.file.Path> moduleSources() throws IOException {
        java.util.List<java.nio.file.Path> sources = new java.util.ArrayList<>();
        for (Module module : MODULES)
            try (java.util.stream.Stream<java.nio.file.Path> walk =
                    Files.walk(Paths.get(module.src()))) {
                walk.filter(f -> f.toString().endsWith(".java")).forEach(sources::add);
            }
        sources.sort(null);
        return sources;
    }

    /** True for the JavaCC output in the parser package. */
    private static boolean isGeneratedParserFile(java.nio.file.Path f) {
        return f.getParent().toString().replace(File.separatorChar, '/').endsWith(PARSER_DIR)
                && f.getFileName().toString().matches(GENERATED_PARSER_FILES);
    }

    /** One jar per module; the shell jar carries the launcher's main class. */
    private static void doJar() {
        doBuild();
        for (Module module : MODULES) {
            if (module.name().equals("shell"))
                createManifest(module.classes() + "/META-INF/MANIFEST.MF", MAIN_CLASS);
            createJar(module.classes(), jarFile(module));
        }
    }

    private static void doDist() {
        doJar();
        String name = "morendo-" + version();
        String stage = BUILDDIR + "/dist/" + name;
        rmTree(BUILDDIR + "/dist");
        mkdir(stage + "/libs");
        for (Module module : MODULES)
            copyForce(jarFile(module), stage + "/libs/" + new File(jarFile(module)).getName());
        for (int i = 0; i < foreignLibs.size(); i++) {
            String lib = foreignLibs.get(i);
            if (!lib.endsWith(JUNIT)) copyForce(lib, stage + "/libs/" + new File(lib).getName());
        }
        for (String f : new String[] {"morendo", "morendo.cmd", "README.md", "LICENSE"})
            copyForce(f, stage + "/" + f);
        makeExecutable(stage + "/morendo");
        copyTree("samples", stage + "/samples");
        copyTree("benchmark", stage + "/benchmark");
        copyTree("licenses", stage + "/licenses");
        String zipFile = BUILDDIR + "/" + name + ".zip";
        zip(BUILDDIR + "/dist", zipFile);
        println("created " + zipFile);
    }

    // ---------------------------------------------------------------- helpers

    /** Runs a task body and turns any failure into a non-zero exit status so that CI notices. */
    private static void guard(Runnable body) {
        try {
            body.run();
        } catch (RuntimeException e) {
            printError(e.getMessage() == null ? e.toString() : e.getMessage());
            System.exit(1);
        }
    }

    /** The arguments that follow the task name on the command line. */
    private static String[] taskArgs() {
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) i++;
        return i + 1 < args.length ? Arrays.copyOfRange(args, i + 1, args.length) : new String[0];
    }

    /**
     * The JUnit console launcher command up to the test selection. The launcher does not expand
     * classpath wildcards, so every jar is listed.
     */
    private static String junitCommand(String systemProperties) {
        StringBuilder cp = new StringBuilder(TEST_CLASSES);
        for (Module module : MODULES) cp.append(File.pathSeparator).append(module.classes());
        for (int i = 0; i < foreignLibs.size(); i++)
            cp.append(File.pathSeparator).append(foreignLibs.get(i));
        return "java "
                + systemProperties
                + " -jar "
                + LIBS
                + "/"
                + JUNIT
                + " execute --disable-banner --details=summary --fail-if-no-tests --class-path "
                + cp
                + " ";
    }

    /** The version string, read from Constants.VERSION so that it is defined in one place. */
    private static String version() {
        try {
            String s = new String(Files.readAllBytes(Paths.get(CONSTANTS)), StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("VERSION\\s*=\\s*\"([^\"]+)\"").matcher(s);
            if (m.find()) return m.group(1);
        } catch (IOException e) {
            // fall through
        }
        throw new RuntimeException("cannot read VERSION from " + CONSTANTS);
    }

    private static String jarFile(Module module) {
        return BUILDDIR + "/morendo-" + module.name() + "-" + version() + ".jar";
    }

    /**
     * The catalog entries with the given short names, as a ForeignDependencies for the build
     * helpers.
     */
    private static ForeignDependencies jars(String... names) {
        ForeignDependencies dep = new ForeignDependencies();
        for (String name : names) {
            boolean found = false;
            for (String[] entry : CATALOG)
                if (entry[0].equals(name)) {
                    dep.add(LIBS, entry[1]);
                    found = true;
                }
            if (!found)
                throw new IllegalArgumentException("no jar named " + name + " in the catalog");
        }
        return dep;
    }

    /** Zips a directory tree, keeping the executable bit of launcher scripts. */
    private static void zip(String rootDir, String zipFile) {
        File root = new File(rootDir);
        try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(new File(zipFile))) {
            addToZip(out, root, root);
        } catch (IOException e) {
            throw new RuntimeException("error creating " + zipFile + ": " + e.getMessage());
        }
    }

    private static void addToZip(ZipArchiveOutputStream out, File root, File dir)
            throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        Arrays.sort(files);
        for (File f : files) {
            if (f.isDirectory()) {
                addToZip(out, root, f);
                continue;
            }
            String name =
                    root.toPath()
                            .relativize(f.toPath())
                            .toString()
                            .replace(File.separatorChar, '/');
            ZipArchiveEntry entry = new ZipArchiveEntry(f, name);
            entry.setUnixMode(f.canExecute() ? 0100755 : 0100644);
            out.putArchiveEntry(entry);
            try (InputStream in = new FileInputStream(f)) {
                in.transferTo(out);
            }
            out.closeArchiveEntry();
        }
    }

    // ---------------------------------------------------------------- dependencies

    /** Build-time tools, kept out of the runtime classpath. */
    private static ForeignDependencies buildToolDependencies() {
        final ForeignDependencies dep = new ForeignDependencies();
        dep.add(TOOLS, MAVEN + "net/java/dev/javacc/javacc/7.0.13/" + JAVACC);
        dep.add(
                TOOLS,
                MAVEN + "com/google/googlejavaformat/google-java-format/1.36.1/" + FORMATTER);
        dep.add(
                TOOLS,
                MAVEN
                        + "com/github/spotbugs/spotbugs/"
                        + SPOTBUGS_VERSION
                        + "/spotbugs-"
                        + SPOTBUGS_VERSION
                        + ".zip");
        return dep;
    }
}
