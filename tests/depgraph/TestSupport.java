package depgraph;


public final class TestSupport {

    @FunctionalInterface
    public interface Operation {

        void run() throws Exception;
    }

    private TestSupport() { }

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
