package depgraph;

import java.util.ArrayList;
import java.util.List;

/** Reads two-column CSV records with quoted fields. */
public final class Csv {
    /** Prevents utility construction. */
    private Csv() { }

    /** Splits a record, accepting doubled quotes inside quoted fields. */
    public static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char symbol = line.charAt(index);
            if (symbol == '"') {
                boolean escaped = quoted && index + 1 < line.length()
                        && line.charAt(index + 1) == '"';
                if (escaped) {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (symbol == ',' && !quoted) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(symbol);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("CSV: unclosed quote");
        }
        fields.add(field.toString().trim());
        return fields;
    }
}
