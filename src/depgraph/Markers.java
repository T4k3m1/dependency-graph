package depgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Markers {
    public static final String PYTHON_VERSION = "3.12.14";
    private static final Map<String, String> ENVIRONMENT = Map.of(
            "python_version", "3.12", "python_full_version", PYTHON_VERSION,
            "os_name", "nt", "sys_platform", "win32", "platform_system", "Windows",
            "platform_machine", "AMD64", "platform_python_implementation", "CPython",
            "implementation_name", "cpython", "implementation_version", PYTHON_VERSION, "extra", "");
    private static final Pattern TOKEN = Pattern.compile(
            "\\s*(?:('[^']*'|\"[^\"]*\"|===|~=|==|!=|<=|>=|[<>()]|[A-Za-z_][A-Za-z_0-9]*))");
    private final List<String> tokens;
    private int position;

    /** Tokenizes a marker expression. */
    private Markers(String expression) {
        tokens = new ArrayList<>();
        int offset = 0;
        while (offset < expression.length()) {
            var matcher = TOKEN.matcher(expression.substring(offset));
            if (!matcher.lookingAt()) {
                throw new IllegalArgumentException("Unsupported marker: " + expression.substring(offset));
            }
            tokens.add(matcher.group(1));
            offset += matcher.end();
        }
    }

    public static boolean applies(String expression) {
        if (expression.isBlank()) {
            return true;
        }
        Markers parser = new Markers(expression.trim());
        boolean result = parser.or();
        if (parser.position != parser.tokens.size()) {
            throw new IllegalArgumentException("Unexpected marker token");
        }
        return result;
    }

    private boolean or() {
        boolean result = and();
        while (take("or")) {
            boolean right = and();
            result = result || right;
        }
        return result;
    }

    private boolean and() {
        boolean result = atom();
        while (take("and")) {
            boolean right = atom();
            result = result && right;
        }
        return result;
    }

    private boolean atom() {
        if (take("(")) {
            boolean result = or();
            if (!take(")")) {
                throw new IllegalArgumentException("Unclosed marker parenthesis");
            }
            return result;
        }
        String left = scalar(next());
        String operator = next();
        if (operator.equals("not")) {
            if (!take("in")) {
                throw new IllegalArgumentException("Expected not in");
            }
            operator = "not in";
        }
        return evaluate(left, operator, scalar(next()));
    }

    private static boolean evaluate(String left, String operator, String right) {
        if (operator.equals("in") || operator.equals("not in")) {
            return operator.equals("in") == right.contains(left);
        }
        if (Versions.stable(left) && Versions.stable(right)) {
            return Versions.matches(left, operator + right);
        }
        return textual(left.compareTo(right), operator);
    }

    private static boolean textual(int order, String operator) {
        return switch (operator) {
            case "==", "===" -> order == 0;
            case "!=" -> order != 0;
            case "<" -> order < 0;
            case ">" -> order > 0;
            case "<=" -> order <= 0;
            case ">=" -> order >= 0;
            default -> throw new IllegalArgumentException("Unsupported marker operator: " + operator);
        };
    }

    private static String scalar(String token) {
        if (token.startsWith("'") || token.startsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }
        if (!ENVIRONMENT.containsKey(token)) {
            throw new IllegalArgumentException("Unsupported marker variable: " + token);
        }
        return ENVIRONMENT.get(token);
    }

    private boolean take(String token) {
        if (position < tokens.size() && tokens.get(position).equals(token)) {
            position++;
            return true;
        }
        return false;
    }

    private String next() {
        if (position == tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of marker");
        }
        return tokens.get(position++);
    }
}
