package depgraph;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Ensures development-stage output does not leak into normal final output. */
public final class CliTests {
    /** Prevents utility construction. */
    private CliTests() { }

    /** Verifies key-value, direct dependencies, BFS and installation-order modes. */
    public static void run() throws Exception {
        String settings = capture("1");
        assert settings.contains("package=A\n") && settings.contains("version=1\n");
        assert settings.contains("output=") && settings.contains("test_mode=true\n");
        assert capture("2").replace("\r", "").equals("B\nC\n");
        assert capture("3").equals("A -> B C\nB -> D\nC -> D\nD ->\n");
        assert capture("4").replace("\r", "").equals("D\nB\nC\nA\n");
    }

    /** Captures output for a fixture stage and always restores System.out. */
    private static String capture(String stage) throws Exception {
        List<String> arguments = new ArrayList<>(List.of(ConfigTests.valid()));
        arguments.addAll(List.of("--stage", stage));
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            Main.execute(arguments.toArray(String[]::new));
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
