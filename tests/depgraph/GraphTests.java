package depgraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GraphTests {
    private static final int WIDE_SIZE = 4000;
    private static final int OVER_DEPTH = 600;

    private GraphTests() { }

    public static void run() throws Exception {
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        Graph graph = Graph.build("A", repository);
        assert new ArrayList<>(graph.edges().keySet()).equals(List.of("A", "B", "C", "D"));
        assert Graph.build("F", repository).edges().equals(Map.of("F", List.of("F")));
        assert Graph.build("G", repository).edges().keySet().equals(java.util.Set.of("G", "H"));
        assert Graph.build("I", repository).edges().equals(Map.of("I", List.of()));
        TestSupport.fails(() -> Graph.build("Z", repository), "Unknown package");
        testWide();
        Repository chain = name -> Integer.parseInt(name) < OVER_DEPTH
                ? List.of(Integer.toString(Integer.parseInt(name) + 1)) : List.of();
        TestSupport.fails(() -> Graph.build("1", chain), "depth");
    }

    private static void testWide() throws Exception {
        Map<String, List<String>> data = new LinkedHashMap<>();
        List<String> children = new ArrayList<>();
        for (int index = 0; index < WIDE_SIZE; index++) {
            String name = "child" + index;
            children.add(name);
            data.put(name, List.of());
        }
        data.put("root", children);
        assert Graph.build("root", data::get).edges().size() == WIDE_SIZE + 1;
    }
}
