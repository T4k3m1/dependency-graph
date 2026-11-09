package depgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestRepository implements Repository {
    private static final int COLUMN_COUNT = 2;
    private final Map<String, List<String>> packages = new LinkedHashMap<>();

    public TestRepository(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        if (lines.isEmpty() || !lines.get(0).equals("package,dependencies")) {
            throw new IllegalArgumentException("Repository header must be package,dependencies");
        }
        for (String line : lines.subList(1, lines.size())) {
            if (!line.isBlank()) {
                addRow(line);
            }
        }
        for (List<String> dependencies : packages.values()) {
            for (String name : dependencies) {
                requirePackage(name);
            }
        }
    }

    private void addRow(String line) {
        List<String> fields = Csv.parse(line);
        if (fields.size() != COLUMN_COUNT || !fields.get(0).matches("[A-Z]+")) {
            throw new IllegalArgumentException("Invalid repository row: " + line);
        }
        List<String> dependencies = fields.get(1).isBlank() ? List.of()
                : Arrays.asList(fields.get(1).split("\\s+"));
        if (dependencies.stream().anyMatch(name -> !name.matches("[A-Z]+"))) {
            throw new IllegalArgumentException("Dependencies must use uppercase Latin names");
        }
        if (packages.putIfAbsent(fields.get(0), dependencies.stream().distinct().toList()) != null) {
            throw new IllegalArgumentException("Duplicate package: " + fields.get(0));
        }
    }

    private void requirePackage(String name) {
        if (!packages.containsKey(name)) {
            throw new IllegalArgumentException("Unknown package: " + name);
        }
    }

    @Override
    public List<String> dependencies(String name) {
        requirePackage(name);
        return packages.get(name);
    }

    public Map<String, List<String>> all() {
        return new LinkedHashMap<>(packages);
    }
}
