package depgraph;

import java.util.Locale;
import java.util.regex.Pattern;

/** Parsed Requires-Dist dependency before choosing its exact version. */
public record Requirement(String name, String constraint) {
    private static final Pattern NAME = Pattern.compile("^([A-Za-z0-9][A-Za-z0-9._-]*)(.*)$");

    /** Applies environment markers and rejects unsupported extras or direct URLs. */
    public static Requirement parse(String text) {
        int separator = text.indexOf(';');
        String declaration = separator < 0 ? text : text.substring(0, separator);
        String marker = separator < 0 ? "" : text.substring(separator + 1);
        if (!Markers.applies(marker)) {
            return null;
        }
        var matcher = NAME.matcher(declaration.trim());
        if (!matcher.matches() || matcher.group(2).contains("[") || matcher.group(2).contains("@")) {
            throw new IllegalArgumentException("Unsupported dependency: " + text);
        }
        return new Requirement(normalize(matcher.group(1)), matcher.group(2).trim());
    }

    /** Implements PyPI name normalization. */
    public static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[-_.]+", "-");
    }
}
