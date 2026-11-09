package depgraph;

/** Exception assertions without a third-party test framework. */
public final class TestSupport {
    /** A checked test operation. */
    @FunctionalInterface
    public interface Operation {
        /** Executes the operation being tested. */
        void run() throws Exception;
    }

    /** Prevents utility construction. */
    private TestSupport() { }

    /** Requires an exception whose diagnostic contains the expected text. */
    public static void fails(Operation operation, String text) throws Exception {
        try {
            operation.run();
        } catch (Exception exception) {
            assert exception.getMessage().contains(text) : exception;
            return;
        }
        throw new AssertionError("Expected exception: " + text);
    }
}
