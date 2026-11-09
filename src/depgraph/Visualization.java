package depgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Visualization {
    private static final long TIMEOUT_SECONDS = 60;
    private static final int SUCCESS_EXIT = 0;

    private Visualization() { }

    public static String d2(Graph graph) {
        StringBuilder result = new StringBuilder("direction: right\n");
        Map<String, String> aliases = new LinkedHashMap<>();
        for (String name : graph.edges().keySet()) {
            String alias = "node" + aliases.size();
            aliases.put(name, alias);
            result.append(alias).append(": \"").append(name).append("\"\n");
        }
        graph.edges().forEach((parent, children) -> children.forEach(child ->
                result.append(aliases.get(parent)).append(" -> ").append(aliases.get(child)).append('\n')));
        return result.toString();
    }

    public static void save(Graph graph, Path output) throws Exception {
        Files.createDirectories(output.getParent());
        String fileName = output.getFileName().toString();
        String stem = fileName.substring(0, fileName.lastIndexOf('.'));
        Path source = output.resolveSibling(stem + ".d2");
        Files.writeString(source, d2(graph));
        Path temporary = Files.createTempFile(output.getParent(), ".d2-render-", ".svg");
        Path log = Files.createTempFile(output.getParent(), ".d2-render-", ".log");
        try {
            render(source, temporary, log);
            if (!Files.readString(temporary).contains("<svg")) {
                throw new IOException("D2 did not produce an SVG document");
            }
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
            Files.deleteIfExists(log);
        }
    }

    private static void render(Path source, Path output, Path log) throws Exception {
        String executable = System.getenv().getOrDefault("D2_BIN", "d2");
        Process process;
        try {
            process = new ProcessBuilder(executable, "--layout", "dagre", source.toString(), output.toString())
                    .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        } catch (IOException exception) {
            throw new IOException("Install D2 in PATH or set D2_BIN to its executable path", exception);
        }
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("D2 renderer timeout");
        }
        if (process.exitValue() != SUCCESS_EXIT) {
            throw new IOException("D2 rendering failed: " + Files.readString(log));
        }
    }
}
