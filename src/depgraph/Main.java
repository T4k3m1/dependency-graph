package depgraph;

/** CLI entry point for variant 11, implemented on Java 17+. */
public final class Main {
    private static final int ERROR_EXIT = 1;

    /** Prevents utility construction. */
    private Main() { }

    /** Reports errors with a nonzero exit code and no stack trace. */
    public static void main(String[] arguments) {
        try {
            execute(arguments);
        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(ERROR_EXIT);
        }
    }

    /** Validates CLI parameters and prints stage-one key-value output. */
    public static void execute(String[] arguments) throws Exception {
        Config config = Config.parse(arguments);
        if (config.stage().equals("1")) {
            System.out.print(config.settings());
            return;
        }
        Repository repository = config.testMode()
                ? new TestRepository(java.nio.file.Path.of(config.repository()))
                : new PypiRepository(config.repository(), config.name(), config.version());
        if (config.stage().equals("2")) {
            repository.dependencies(config.root()).forEach(System.out::println);
        } else {
            Graph graph = Graph.build(config.root(), repository);
            if (config.stage().equals("4")) {
                InstallOrder.of(graph).forEach(System.out::println);
            } else if (config.stage().equals("3")) {
                System.out.print(graph.adjacency());
            } else {
                Visualization.save(graph, config.output());
                System.out.print(Visualization.d2(graph));
            }
        }
    }
}
