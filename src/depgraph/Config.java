package depgraph;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Five required CLI settings plus the demonstration stage selector. */
public record Config(String name, String repository, boolean testMode, String version,
                     Path output, String stage) {
    private static final int PAIR_SIZE = 2;
    private static final Set<String> REQUIRED = Set.of(
            "--package", "--repository", "--test-mode", "--version", "--output");

    /** Parses pairs of option names and values, rejecting unknown or repeated keys. */
    public static Config parse(String[] arguments) {
        Map<String, String> values = new LinkedHashMap<>();
        if (arguments.length % PAIR_SIZE != 0) {
            throw new IllegalArgumentException("Each CLI option requires a value");
        }
        for (int index = 0; index < arguments.length; index += PAIR_SIZE) {
            String key = arguments[index];
            String value = arguments[index + 1];
            if ((!REQUIRED.contains(key) && !key.equals("--stage")) || value.isBlank()) {
                throw new IllegalArgumentException("Unknown option or empty value: " + key);
            }
            if (values.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Repeated option: " + key);
            }
        }
        if (!values.keySet().containsAll(REQUIRED)) {
            throw new IllegalArgumentException("Required options: " + REQUIRED);
        }
        return validated(values);
    }

    /** Validates every setting before any HTTP request or output creation. */
    private static Config validated(Map<String, String> values) {
        String mode = values.get("--test-mode");
        if (!Set.of("true", "false").contains(mode)) {
            throw new IllegalArgumentException("--test-mode must be true or false");
        }
        boolean test = Boolean.parseBoolean(mode);
        String name = values.get("--package");
        String pattern = test ? "[A-Z]+" : "[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?";
        if (!name.matches(pattern)) {
            throw new IllegalArgumentException("Invalid --package: " + name);
        }
        String version = values.get("--version");
        if (!version.matches("[0-9]+(?:\\.[0-9]+)*")) {
            throw new IllegalArgumentException("--version must be an exact stable numeric release");
        }
        String stage = values.getOrDefault("--stage", "5");
        if (!Set.of("1", "2", "3", "4", "5").contains(stage)) {
            throw new IllegalArgumentException("--stage must be 1..5");
        }
        return new Config(name, repository(values.get("--repository"), test), test,
                version, output(values.get("--output")), stage);
    }

    /** Checks a readable fixture path or a PyPI-compatible JSON API base URL. */
    private static String repository(String value, boolean test) {
        if (test) {
            if (!Files.isRegularFile(Path.of(value)) || !Files.isReadable(Path.of(value))) {
                throw new IllegalArgumentException("Unreadable --repository file: " + value);
            }
            return value;
        }
        URI uri = URI.create(value);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getQuery() != null
                || uri.getFragment() != null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("--repository must be an HTTP(S) base URL");
        }
        return value.replaceAll("/+$", "");
    }

    /** Checks the SVG extension, target type and nearest existing writable parent. */
    private static Path output(String value) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!value.toLowerCase(java.util.Locale.ROOT).endsWith(".svg") || Files.isDirectory(path)) {
            throw new IllegalArgumentException("--output must name an SVG file");
        }
        Path parent = path.getParent();
        while (parent != null && !Files.exists(parent)) {
            parent = parent.getParent();
        }
        if (parent == null || !Files.isDirectory(parent)) {
            throw new IllegalArgumentException("--output parent is not writable");
        }
        try {
            Path probe = Files.createTempFile(parent, ".write-test-", ".tmp");
            Files.delete(probe);
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException("--output parent is not writable", exception);
        }
        return path;
    }

    /** Shows the five user-configurable parameters for stage one only. */
    public String settings() {
        return "package=" + name + "\nrepository=" + repository + "\ntest_mode=" + testMode
                + "\nversion=" + version + "\noutput=" + output + "\n";
    }
}
