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
        if (config.testMode()) {
            throw new IllegalArgumentException("Stage 2 requires a PyPI repository");
        }
        Repository repository = new PypiRepository(config.repository(), config.name(), config.version());
        String root = Requirement.normalize(config.name()) + "==" + config.version();
        repository.dependencies(root).forEach(System.out::println);
    }
}
