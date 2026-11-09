package depgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small strict JSON reader implemented without third-party libraries. */
public final class Json {
    private static final int HEX_LENGTH = 4;
    private static final int HEX_RADIX = 16;
    private static final int MAX_DEPTH = 128;
    private static final Map<Character, Character> ESCAPES = Map.of(
            '"', '"', '\\', '\\', '/', '/', 'b', '\b',
            'f', '\f', 'n', '\n', 'r', '\r', 't', '\t');
    private final String input;
    private int position;
    private int depth;

    /** Stores the source document. */
    private Json(String input) {
        this.input = input;
    }

    /** Parses exactly one JSON value and rejects trailing data. */
    public static Object parse(String input) {
        Json parser = new Json(input);
        Object value = parser.value();
        parser.space();
        if (parser.position != input.length()) {
            throw new IllegalArgumentException("Trailing JSON data");
        }
        return value;
    }

    /** Reads a value with a bounded nesting depth. */
    private Object value() {
        space();
        if (++depth > MAX_DEPTH || position == input.length()) {
            throw new IllegalArgumentException("Invalid JSON depth or unexpected end");
        }
        Object result = switch (input.charAt(position)) {
            case '{' -> objectValue();
            case '[' -> arrayValue();
            case '"' -> string();
            case 't' -> literal("true", Boolean.TRUE);
            case 'f' -> literal("false", Boolean.FALSE);
            case 'n' -> literal("null", null);
            default -> number();
        };
        depth--;
        return result;
    }

    /** Reads unique string keys and their values. */
    private Map<String, Object> objectValue() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        if (take('}')) {
            return result;
        }
        do {
            space();
            String key = string();
            expect(':');
            if (result.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate JSON key: " + key);
            }
            result.put(key, value());
        } while (take(','));
        expect('}');
        return result;
    }

    /** Reads an array, preserving element order. */
    private List<Object> arrayValue() {
        expect('[');
        List<Object> result = new ArrayList<>();
        if (take(']')) {
            return result;
        }
        do {
            result.add(value());
        } while (take(','));
        expect(']');
        return result;
    }

    /** Reads JSON string escapes and rejects unterminated strings. */
    private String string() {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (position < input.length()) {
            char symbol = input.charAt(position++);
            if (symbol == '"') {
                return result.toString();
            }
            if (Character.isISOControl(symbol)) {
                throw new IllegalArgumentException("Control character in JSON string");
            }
            result.append(symbol == '\\' ? escape() : symbol);
        }
        throw new IllegalArgumentException("Unclosed JSON string");
    }

    /** Decodes a single escaped character. */
    private char escape() {
        char symbol = escapedSymbol();
        if (symbol == 'u') {
            return unicode();
        }
        Character decoded = ESCAPES.get(symbol);
        if (decoded == null) {
            throw new IllegalArgumentException("Invalid JSON escape");
        }
        return decoded;
    }

    /** Requires the character following a backslash. */
    private char escapedSymbol() {
        if (position == input.length()) {
            throw new IllegalArgumentException("Incomplete JSON escape");
        }
        return input.charAt(position++);
    }

    /** Decodes four hexadecimal digits. */
    private char unicode() {
        if (position + HEX_LENGTH > input.length()) {
            throw new IllegalArgumentException("Incomplete unicode escape");
        }
        String digits = input.substring(position, position + HEX_LENGTH);
        position += HEX_LENGTH;
        return (char) Integer.parseInt(digits, HEX_RADIX);
    }

    /** Parses JSON numbers as BigDecimal, rejecting non-JSON spellings. */
    private Object number() {
        var matcher = java.util.regex.Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
                .matcher(input.substring(position));
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException("Invalid JSON value at " + position);
        }
        position += matcher.end();
        return new java.math.BigDecimal(matcher.group());
    }

    /** Reads a fixed JSON literal. */
    private Object literal(String spelling, Object value) {
        if (!input.startsWith(spelling, position)) {
            throw new IllegalArgumentException("Invalid JSON literal");
        }
        position += spelling.length();
        return value;
    }

    /** Skips JSON whitespace. */
    private void space() {
        while (position < input.length() && " \r\n\t".indexOf(input.charAt(position)) >= 0) {
            position++;
        }
    }

    /** Consumes an optional punctuation character. */
    private boolean take(char symbol) {
        space();
        if (position < input.length() && input.charAt(position) == symbol) {
            position++;
            return true;
        }
        return false;
    }

    /** Requires a punctuation character. */
    private void expect(char symbol) {
        if (!take(symbol)) {
            throw new IllegalArgumentException("JSON expected " + symbol + " at " + position);
        }
    }

    /** Requires an object and exposes its keys without unsafe generic casts. */
    public static Map<?, ?> object(Object value) {
        if (!(value instanceof Map<?, ?> object)) {
            throw new IllegalArgumentException("Expected JSON object");
        }
        return object;
    }

    /** Requires an array, treating a null requires_dist value as an empty list. */
    public static List<?> array(Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> array)) {
            throw new IllegalArgumentException("Expected JSON array");
        }
        return array;
    }
}
