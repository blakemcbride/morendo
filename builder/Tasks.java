package builder;

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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import static builder.BuildUtils.*;

/**
 * Build tasks for Morendo, run through ./bld (bld.cmd on Windows). The generic helpers live
 * in builder/BuildUtils.java and are not meant to be edited; this file is the whole build.
 *
 * Layout:
 *   src/main/java        engine sources (package org.jamocha)
 *   src/main/resources   icons and message bundles, copied next to the classes
 *   src/main/javacc      the CLIPS grammar; the parser is generated into
 *                        src/main/java/org/jamocha/parser/clips (git-ignored there)
 *   src/test/java        tests, sample beans and example code
 *   src/test/resources   golden files and scenario scripts for the characterization tests
 *   libs/                downloaded jars (libs/tools holds build-time tools such as JavaCC)
 *   target/              everything the build produces
 *
 * The version number lives in one place: Constants.VERSION in src/main/java.
 */
public class Tasks {

	final static String LIBS = "libs";
	final static String TOOLS = LIBS + "/tools";
	final static String BUILDDIR = "target";
	final static String CLASSES = BUILDDIR + "/classes";
	final static String TEST_CLASSES = BUILDDIR + "/test-classes";
	final static String MAIN_SRC = "src/main/java";
	final static String TEST_SRC = "src/test/java";
	final static String RESOURCES = "src/main/resources";
	final static String GRAMMAR = "src/main/javacc/clips.jj";
	final static String PARSER_DIR = MAIN_SRC + "/org/jamocha/parser/clips";
	final static String GENERATED_PARSER_FILES = "CLIPSParser.*\\.java|ParseException\\.java|SimpleCharStream\\.java|Token\\.java|TokenMgrError\\.java";
	final static String CONSTANTS = MAIN_SRC + "/org/jamocha/rete/Constants.java";
	final static String MAIN_CLASS = "org.jamocha.Morendo";
	final static String TEST_SUITE = "org.jamocha.AllTests";
	final static String GOLDEN_TESTS = "org.jamocha.golden.GoldenSampleTest";
	final static String JUNIT_RUNNER = "org.junit.runner.JUnitCore";
	final static String MAVEN = "https://repo1.maven.org/maven2/";
	final static String JAVACC = "javacc-7.0.13.jar";
	final static String JUNIT = "junit-4.1.jar";

	final static ForeignDependencies foreignLibs = buildForeignDependencies();
	final static ForeignDependencies toolLibs = buildToolDependencies();
	final static LocalDependencies localLibs = new LocalDependencies();

	private static String[] args;

	public static void main(String[] args) throws Exception {
		Tasks.args = args;
		BuildUtils.build(args, Tasks.class, LIBS);
	}

	public static void listTasks() {
		println("");
		println("build                    download dependencies, generate the parser, compile");
		println("test [class]             build and run the test suite (org.jamocha.AllTests), or one test class");
		println("golden-update [names]    regenerate the golden files (all, or a comma-separated list of scenarios)");
		println("run <class> [argument]... build and run a class, e.g. bld run org.jamocha.Morendo -gui");
		println("                         (for the interactive shell use ./morendo -shell instead)");
		println("parser                   regenerate the CLIPS parser from src/main/javacc/clips.jj if it changed");
		println("jar                      build target/morendo-<version>.jar");
		println("dist                     build target/morendo-<version>.zip (jar, libs, launcher, samples)");
		println("javadoc                  build target/javadoc");
		println("libs                     download the jar files into libs/");
		println("");
		println("clean                    remove target/ and the generated parser sources");
		println("realclean                + remove downloaded jar files");
		println("ideclean                 + IDE files");
		println("");
	}

	// ---------------------------------------------------------------- tasks

	public static void libs() {
		guard(() -> {
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
		guard(() -> {
			doBuildTests();
			String[] targs = taskArgs();
			String suite = targs.length > 0 ? targs[0] : TEST_SUITE;
			runWait(true, "java -cp " + testClasspath() + " " + JUNIT_RUNNER + " " + suite);
		});
	}

	public static void goldenUpdate() {
		guard(() -> {
			doBuildTests();
			String[] targs = taskArgs();
			String only = targs.length > 0 ? " -Dgolden.only=" + targs[0] : "";
			runWait(true, "java -Dgolden.update=true" + only + " -cp " + testClasspath() + " " + JUNIT_RUNNER + " " + GOLDEN_TESTS);
			println("golden files written to src/test/resources/golden; review the diff before committing");
		});
	}

	public static void run() {
		guard(() -> {
			doBuild();
			runJava(CLASSES, localLibs, foreignLibs);
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
			runWait(true, "java -cp " + TOOLS + "/" + JAVACC + " javacc -OUTPUT_DIRECTORY=" + PARSER_DIR + " " + GRAMMAR);
		}
	}

	private static void doBuild() {
		downloadAll(foreignLibs);
		doParser();
		mkdir(CLASSES);
		buildJava(MAIN_SRC, CLASSES, localLibs, foreignLibs, null);
		copyTree(RESOURCES, CLASSES);
	}

	private static void doBuildTests() {
		doBuild();
		mkdir(TEST_CLASSES);
		buildJava(TEST_SRC, TEST_CLASSES, localLibs, foreignLibs, CLASSES);
	}

	/**
	 * BuildUtils.buildJavadoc cannot pass options, and the 2000s-era comments in this code
	 * base fail doclint's HTML checks, so javadoc is invoked directly with doclint off.
	 */
	private static void doJavadoc() {
		downloadAll(foreignLibs);
		doParser();
		String dest = BUILDDIR + "/javadoc";
		try {
			java.util.List<java.nio.file.Path> sources;
			try (java.util.stream.Stream<java.nio.file.Path> walk = Files.walk(Paths.get(MAIN_SRC))) {
				sources = walk.filter(f -> f.toString().endsWith(".java")).sorted().collect(java.util.stream.Collectors.toList());
			}
			long newest = 0;
			for (java.nio.file.Path f : sources)
				newest = Math.max(newest, f.toFile().lastModified());
			File index = new File(dest, "index.html");
			if (index.exists() && index.lastModified() >= newest)
				return;
			mkdir(dest);
			File argsFile = File.createTempFile("javadoc-", ".args");
			StringBuilder sb = new StringBuilder();
			for (java.nio.file.Path f : sources)
				sb.append(f).append('\n');
			Files.write(argsFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
			runWait(true, "javadoc -Xdoclint:none -quiet -d " + dest + " -cp " + LIBS + "/* @" + argsFile);
			rm(argsFile.getPath());
		} catch (IOException e) {
			throw new RuntimeException("javadoc: " + e.getMessage());
		}
	}

	private static void doJar() {
		doBuild();
		createManifest(CLASSES + "/META-INF/MANIFEST.MF", MAIN_CLASS);
		createJar(CLASSES, jarFile());
	}

	private static void doDist() {
		doJar();
		String name = "morendo-" + version();
		String stage = BUILDDIR + "/dist/" + name;
		rmTree(BUILDDIR + "/dist");
		mkdir(stage + "/libs");
		copyForce(jarFile(), stage + "/morendo.jar");
		for (int i = 0; i < foreignLibs.size(); i++) {
			String lib = foreignLibs.get(i);
			if (!lib.endsWith(JUNIT))
				copyForce(lib, stage + "/libs/" + new File(lib).getName());
		}
		for (String f : new String[] { "morendo", "morendo.cmd", "log4j.properties", "README.md", "LICENSE" })
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
		while (i < args.length && args[i].startsWith("-"))
			i++;
		return i + 1 < args.length ? Arrays.copyOfRange(args, i + 1, args.length) : new String[0];
	}

	private static String testClasspath() {
		String sep = File.pathSeparator;
		return CLASSES + sep + TEST_CLASSES + sep + LIBS + "/*";
	}

	/** The version string, read from Constants.VERSION so that it is defined in one place. */
	private static String version() {
		try {
			String s = new String(Files.readAllBytes(Paths.get(CONSTANTS)), StandardCharsets.UTF_8);
			Matcher m = Pattern.compile("VERSION\\s*=\\s*\"([^\"]+)\"").matcher(s);
			if (m.find())
				return m.group(1);
		} catch (IOException e) {
			// fall through
		}
		throw new RuntimeException("cannot read VERSION from " + CONSTANTS);
	}

	private static String jarFile() {
		return BUILDDIR + "/morendo-" + version() + ".jar";
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

	private static void addToZip(ZipArchiveOutputStream out, File root, File dir) throws IOException {
		File[] files = dir.listFiles();
		if (files == null)
			return;
		Arrays.sort(files);
		for (File f : files) {
			if (f.isDirectory()) {
				addToZip(out, root, f);
				continue;
			}
			String name = root.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '/');
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

	private static ForeignDependencies buildForeignDependencies() {
		final ForeignDependencies dep = new ForeignDependencies();
		// runtime
		dep.add(LIBS, MAVEN + "log4j/log4j/1.2.14/log4j-1.2.14.jar");
		dep.add(LIBS, MAVEN + "com/fasterxml/jackson/core/jackson-core/2.12.3/jackson-core-2.12.3.jar");
		dep.add(LIBS, MAVEN + "com/fasterxml/jackson/core/jackson-databind/2.12.3/jackson-databind-2.12.3.jar");
		dep.add(LIBS, MAVEN + "com/fasterxml/jackson/core/jackson-annotations/2.12.3/jackson-annotations-2.12.3.jar");
		dep.add(LIBS, MAVEN + "javax/jms/jms-api/1.1-rev-1/jms-api-1.1-rev-1.jar");
		dep.add(LIBS, MAVEN + "javax/servlet/servlet-api/2.5/servlet-api-2.5.jar");
		// tests only; dist() leaves it out of the distribution
		dep.add(LIBS, MAVEN + "junit/junit/4.1/" + JUNIT);
		return dep;
	}

	/** Build-time tools, kept out of the runtime classpath. */
	private static ForeignDependencies buildToolDependencies() {
		final ForeignDependencies dep = new ForeignDependencies();
		dep.add(TOOLS, MAVEN + "net/java/dev/javacc/javacc/7.0.13/" + JAVACC);
		return dep;
	}
}
