package depgraph;

import java.nio.file.Path;
import java.util.List;

public final class OrderTests {

    private OrderTests() { }

    public static void run() throws Exception {
        TestRepository repository = new TestRepository(Path.of("examples/repository.csv"));
        assert InstallOrder.of(Graph.build("A", repository)).equals(List.of("D", "B", "C", "A"));
        assert InstallOrder.of(Graph.build("E", repository)).equals(List.of("D", "B", "C", "A", "E"));
        assert InstallOrder.of(Graph.build("I", repository)).equals(List.of("I"));
        TestSupport.fails(() -> InstallOrder.of(Graph.build("G", repository)), "cyclic");
        TestSupport.fails(() -> InstallOrder.of(Graph.build("F", repository)), "cyclic");
    }
}
