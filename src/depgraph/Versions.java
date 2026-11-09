package depgraph;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class Versions {
    private static final Pattern CONDITION = Pattern.compile(
            "(===|~=|==|!=|<=|>=|<|>)([0-9]+(?:\\.[0-9]+)*(?:\\.\\*)?)");
    private static final int MIN_COMPATIBLE_PARTS = 2;
    private static final int COMPATIBLE_OFFSET = 2;

    private Versions() { }

    public static boolean stable(String version) {
        return version.matches("[0-9]+(?:\\.[0-9]+)*");
    }

    public static int compare(String left, String right) {
        if (!stable(left) || !stable(right)) {
            throw new IllegalArgumentException("Unsupported nonnumeric version: " + left + " / " + right);
        }
        String[] first = left.split("\\.");
        String[] second = right.split("\\.");
        for (int index = 0; index < Math.max(first.length, second.length); index++) {
            BigInteger a = index < first.length ? new BigInteger(first[index]) : BigInteger.ZERO;
            BigInteger b = index < second.length ? new BigInteger(second[index]) : BigInteger.ZERO;
            int result = a.compareTo(b);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public static boolean matches(String version, String expression) {
        String cleaned = expression.replace("(", "").replace(")", "").replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            return true;
        }
        for (String condition : cleaned.split(",")) {
            if (!condition(version, condition)) {
                return false;
            }
        }
        return true;
    }

    private static boolean condition(String version, String expression) {
        var matcher = CONDITION.matcher(expression);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported version constraint: " + expression);
        }
        String operator = matcher.group(1);
        String bound = matcher.group(2);
        if (bound.endsWith(".*")) {
            return wildcard(version, operator, bound.substring(0, bound.length() - MIN_COMPATIBLE_PARTS));
        }
        return bounded(version, operator, bound);
    }

    private static boolean bounded(String version, String operator, String bound) {
        int order = compare(version, bound);
        return switch (operator) {
            case "==", "===" -> order == 0;
            case "!=" -> order != 0;
            case ">=" -> order >= 0;
            case "<=" -> order <= 0;
            case ">" -> order > 0;
            case "<" -> order < 0;
            case "~=" -> order >= 0 && compare(version, upperCompatible(bound)) < 0;
            default -> throw new IllegalArgumentException("Unsupported operator");
        };
    }

    private static boolean wildcard(String version, String operator, String prefix) {
        boolean equal = version.equals(prefix) || version.startsWith(prefix + ".");
        return switch (operator) {
            case "==" -> equal;
            case "!=" -> !equal;
            default -> throw new IllegalArgumentException("Wildcard requires == or !=");
        };
    }

    /** Calculates the exclusive upper bound of a compatible-release constraint. */
    private static String upperCompatible(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < MIN_COMPATIBLE_PARTS) {
            throw new IllegalArgumentException("~= requires at least two release segments");
        }
        int last = parts.length - COMPATIBLE_OFFSET;
        parts[last] = new BigInteger(parts[last]).add(BigInteger.ONE).toString();
        return String.join(".", Arrays.copyOf(parts, last + 1));
    }
}
