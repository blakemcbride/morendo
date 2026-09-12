package woolfel.rete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.morendo.rete.Rete;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Output routing: console writers see the terminal, a named router sees only its own output. */
public class RouterOutputTest {

    static void load(Rete engine, String text) {
        engine.loadRuleset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void namedRouterKeepsItsOutputToItself() {
        Rete engine = new Rete();
        try {
            StringWriter console = new StringWriter();
            StringWriter named = new StringWriter();
            engine.addPrintWriter("console", console);
            engine.openRouter("log", named);
            engine.writeMessage("to the terminal");
            engine.writeMessage("to the log", "log");
            engine.writeMessage("elsewhere", "nowhere");
            assertEquals("to the terminalelsewhere", console.toString());
            assertEquals("to the log", named.toString());
            assertTrue(engine.closeRouter("log"));
            assertFalse(engine.closeRouter("log"), "closing twice finds nothing");
            engine.writeMessage("after close", "log");
            assertEquals("to the log", named.toString());
            assertEquals("to the terminalelsewhereafter close", console.toString());
        } finally {
            engine.close();
        }
    }

    @Test
    public void fileRoutersRoundTrip() throws Exception {
        Rete engine = new Rete();
        Path file = Files.createTempFile("morendo-router", ".txt");
        try {
            engine.openRouter("out", file.toString(), "w");
            load(engine, "(printout out \"line one\" crlf) (format out \"%s %d%n\" \"two\" 2)");
            engine.closeRouter("out");
            engine.openRouter("out", file.toString(), "a");
            engine.writeMessage("three" + System.lineSeparator(), "out");
            engine.closeRouter(null);
            engine.openRouter("in", file.toString(), "r");
            assertEquals("line one", engine.readLine("in"));
            assertEquals("two 2", engine.readLine("in"));
            assertEquals("three", engine.readLine("in"));
            assertNull(engine.readLine("in"), "null at the end of the file");
            engine.closeRouter("in");
        } finally {
            engine.close();
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void inputSupplierFeedsTheTerminal() {
        Rete engine = new Rete();
        try {
            java.util.Iterator<String> lines = java.util.List.of("first", "second 2").iterator();
            engine.setInputSupplier(() -> lines.hasNext() ? lines.next() : null);
            StringWriter console = new StringWriter();
            engine.addPrintWriter("console", console);
            load(engine, "(bind ?a (readline t)) (bind ?b (read t)) (printout t ?a \"|\" ?b crlf)");
            assertEquals("first|second" + System.lineSeparator(), console.toString());
            assertEquals("EOF", engine.readLine("t") == null ? "EOF" : "line");
        } finally {
            engine.close();
        }
    }
}
