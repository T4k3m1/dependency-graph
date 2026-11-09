package depgraph;

/**
 * Provides small assertion helpers for tests without a third-party framework.
 */
public final class TestSupport {
    /**
     * Represents an operation that may fail with a checked exception.
     */
    @FunctionalInterface
    public interface Operation {
        /**
         * Executes the operation under test.
         *
         * @throws Exception if the operation fails
         */
        void run() throws Exception;
    }

    /**
     * Prevents instances of this utility class.
     */
    private TestSupport() { }

    /**
     * Verifies that an operation throws an exception with the expected text.
     *
     * @param operation operation expected to fail
     * @param text expected text in the exception message
     * @throws Exception if the operation throws an unexpected checked exception
     * @throws AssertionError if no exception is thrown or its message differs
     */
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
