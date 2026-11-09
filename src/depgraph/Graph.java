package depgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Directed dependency graph whose insertion order is recursive BFS order. */
public record Graph(Map<String, List<String>> edges) {
    private static final int MAX_LEVELS = 512;

    /** Starts BFS, marking vertices when discovered to handle shared and cyclic edges. */
    public static Graph build(String root, Repository repository) throws Exception {
        Map<String, List<String>> edges = new LinkedHashMap<>();
        Set<String> discovered = new LinkedHashSet<>();
        discovered.add(root);
        level(List.of(root), repository, edges, discovered, 0);
        return new Graph(edges);
    }

    /** Recursively processes one complete BFS layer before the next layer. */
    private static void level(List<String> frontier, Repository repository,
                              Map<String, List<String>> edges, Set<String> discovered,
                              int depth) throws Exception {
        if (depth >= MAX_LEVELS) {
            throw new IllegalArgumentException("Recursive BFS exceeds supported depth " + MAX_LEVELS);
        }
        List<String> next = new ArrayList<>();
        for (String vertex : frontier) {
            List<String> children = repository.dependencies(vertex);
            edges.put(vertex, List.copyOf(children));
            for (String child : children) {
                if (discovered.add(child)) {
                    next.add(child);
                }
            }
        }
        if (!next.isEmpty()) {
            level(next, repository, edges, discovered, depth + 1);
        }
    }

    /** Prints the stage-three adjacency list in BFS order. */
    public String adjacency() {
        StringBuilder result = new StringBuilder();
        edges.forEach((name, children) -> result.append(name).append(" -> ")
                .append(String.join(" ", children)).append('\n'));
        return result.toString();
    }
}
