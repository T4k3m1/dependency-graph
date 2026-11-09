package depgraph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** D2 golden outputs and optional real-renderer integration checks. */
public final class VisualizationTests {
    /** Prevents utility construction. */
    private VisualizationTests() { }

    /** Checks isolated vertices, edge direction, shared descendants and cycles. */
    public static void run() throws Exception {
        Graph leaf = new Graph(Map.of("I", List.of()));
        assert Visualization.d2(leaf).equals("direction: right\nnode0: \"I\"\n");
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        String diamond = Visualization.d2(Graph.build("A", repository));
        assert diamond.contains("node0 -> node1\n");
        assert diamond.contains("node1 -> node3\n");
        assert diamond.contains("node2 -> node3\n");
        assert Visualization.d2(Graph.build("F", repository)).contains("node0 -> node0\n");
        String cycle = Visualization.d2(Graph.build("G", repository));
        assert cycle.contains("node0 -> node1\n") && cycle.contains("node1 -> node0\n");
    }

    /** Exports actual SVGs for a cycle and isolated node using the installed D2 executable. */
    public static void integration() throws Exception {
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        Path folder = Path.of("build", "svg tests with spaces").toAbsolutePath();
        for (String root : List.of("A", "F", "G", "I")) {
            Path output = folder.resolve(root + ".svg");
            Visualization.save(Graph.build(root, repository), output);
            String image = Files.readString(output);
            assert image.contains("<svg") && image.contains(root);
            assert Files.isRegularFile(folder.resolve(root + ".d2"));
        }
    }
}
