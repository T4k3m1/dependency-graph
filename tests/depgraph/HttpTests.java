package depgraph;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Offline PyPI HTTP integration tests on an automatically selected localhost port. */
public final class HttpTests {
    private static final int AUTO_PORT = 0;
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;

    /** Prevents utility construction. */
    private HttpTests() { }

    /** Tests exact root versions, release filtering, marker exclusion and HTTP errors. */
    public static void run() throws Exception {
        Map<String, String> fixtures = fixtures();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", AUTO_PORT), AUTO_PORT);
        server.createContext("/", exchange -> {
            String value = fixtures.get(exchange.getRequestURI().getPath());
            byte[] content = (value == null ? "not found" : value).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(value == null ? NOT_FOUND : OK, content.length);
            try (var output = exchange.getResponseBody()) {
                output.write(content);
            }
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            PypiRepository repository = new PypiRepository(base, "demo", "1");
            assert repository.dependencies("demo==1").equals(List.of("leaf==1.5"));
            assert repository.dependencies("demo==1").equals(List.of("leaf==1.5"));
            TestSupport.fails(() -> repository.dependencies("absent==1"), "HTTP 404");
            TestSupport.fails(() -> repository.dependencies("broken==1"), "JSON");
            TestSupport.fails(() -> repository.dependencies("wrong==1"), "different package version");
            TestSupport.fails(() -> repository.dependencies("conflict==1"), "Version conflict");
        } finally {
            server.stop(AUTO_PORT);
        }
    }

    /** Defines candidate versions including yanked and Python-incompatible releases. */
    private static Map<String, String> fixtures() {
        String demo = "{\"info\":{\"version\":\"1\",\"requires_python\":\">=3.12\","
                + "\"requires_dist\":[\"Leaf>=1,<3\",\"optional; extra == 'feature'\"]}}";
        String releases = "{\"releases\":{\"1\":[{}],\"1.5\":[{}],"
                + "\"2\":[{\"requires_python\":\">=3.13\"}],\"1.9\":[{\"yanked\":true}]}}";
        return Map.of("/pypi/demo/1/json", demo, "/pypi/leaf/json", releases,
                "/pypi/broken/1/json", "broken JSON",
                "/pypi/wrong/1/json", "{\"info\":{\"version\":\"2\"}}",
                "/pypi/conflict/1/json", "{\"info\":{\"version\":\"1\",\"requires_dist\":[\"demo<1\"]}}");
    }
}
