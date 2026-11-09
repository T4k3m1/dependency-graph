package depgraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class InstallOrder {

    private InstallOrder() { }

    public static List<String> of(Graph graph) {
        Map<String, Integer> remaining = new LinkedHashMap<>();
        Map<String, List<String>> parents = new LinkedHashMap<>();
        graph.edges().forEach((name, children) -> {
            remaining.put(name, children.size());
            children.forEach(child -> parents.computeIfAbsent(child, key -> new ArrayList<>()).add(name));
        });
        PriorityQueue<String> ready = new PriorityQueue<>();
        remaining.forEach((name, count) -> {
            if (count == 0) {
                ready.add(name);
            }
        });
        List<String> result = new ArrayList<>();
        while (!ready.isEmpty()) {
            String name = ready.remove();
            result.add(name);
            for (String parent : parents.getOrDefault(name, List.of())) {
                int count = remaining.compute(parent, (key, value) -> value - 1);
                if (count == 0) {
                    ready.add(parent);
                }
            }
        }
        if (result.size() != graph.edges().size()) {
            throw new IllegalArgumentException("No dependency-first total order: cyclic dependencies");
        }
        return result;
    }
}
