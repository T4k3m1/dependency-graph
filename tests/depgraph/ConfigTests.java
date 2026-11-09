package depgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Error coverage for all five required command-line settings. */
public final class ConfigTests {
    /** Prevents utility construction. */
    private ConfigTests() { }

    /** Returns fresh valid fixture arguments for reuse in test groups. */
    public static String[] valid() {
        return new String[] {"--package", "A", "--repository", "examples/repository.csv",
                "--test-mode", "true", "--version", "1", "--output", "build/test-output.svg"};
    }

    /** Replaces an option value while preserving all other settings. */
    public static String[] replace(String key, String value) {
        List<String> arguments = new ArrayList<>(List.of(valid()));
        arguments.set(arguments.indexOf(key) + 1, value);
        return arguments.toArray(String[]::new);
    }

    /** Verifies invalid package, repository, mode, version, output and option syntax. */
    public static void run() throws Exception {
        Map<String, String> invalid = new LinkedHashMap<>();
        invalid.put("--package", "lower");
        invalid.put("--repository", "examples/absent.csv");
        invalid.put("--test-mode", "yes");
        invalid.put("--version", "latest");
        invalid.put("--output", "build/result.png");
        for (var item : invalid.entrySet()) {
            TestSupport.fails(() -> Config.parse(replace(item.getKey(), item.getValue())), item.getKey());
        }
        TestSupport.fails(() -> Config.parse(replace("--output", "src.svg/..")), "--output");
        TestSupport.fails(() -> Config.parse(new String[] {}), "Required");
        TestSupport.fails(() -> Config.parse(new String[] {"--package"}), "value");
        TestSupport.fails(() -> Config.parse(new String[] {"--unknown", "1"}), "Unknown");
        List<String> duplicate = new ArrayList<>(List.of(valid()));
        duplicate.addAll(List.of("--package", "B"));
        TestSupport.fails(() -> Config.parse(duplicate.toArray(String[]::new)), "Repeated");
        TestSupport.fails(() -> Json.parse("{\"x\":1,\"x\":2}"), "Duplicate");
        TestSupport.fails(() -> Json.parse("[] trailing"), "Trailing");
        TestSupport.fails(() -> Versions.matches("1", ">=1rc1"), "Unsupported");
        TestSupport.fails(() -> Markers.applies("unknown == 'x'"), "Unsupported");
        TestSupport.fails(() -> Requirement.parse("name[feature]>=1"), "Unsupported");
    }
}
