package depgraph;

/** Dependency-free Java assertion test runner. */
public final class AllTests {
    /** Prevents utility construction. */
    private AllTests() { }

    /** Runs all available test groups with assertions enabled by the launcher. */
    public static void main(String[] arguments) throws Exception {
        Config config = Config.parse(new String[] {"--package", "requests", "--version", "2.32.3",
                "--repository", "https://pypi.org", "--test-mode", "false", "--output", "generated/test.svg"});
        assert config.name().equals("requests");
        assert config.stage().equals("5");
        assert config.settings().contains("version=2.32.3\n");
        MetadataTests.run();
        GraphTests.run();
        System.out.println("PASS: CLI, metadata, recursive BFS");
    }
}
