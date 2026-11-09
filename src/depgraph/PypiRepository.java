package depgraph;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PypiRepository implements Repository {
    private static final int HTTP_OK = 200;
    private static final int ID_PARTS = 2;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private final String base;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Map<String, Map<?, ?>> cache = new HashMap<>();
    private final Map<String, String> selected = new LinkedHashMap<>();

    public PypiRepository(String base, String root, String version) {
        this.base = base;
        selected.put(Requirement.normalize(root), version);
    }

    @Override
    public List<String> dependencies(String identity) throws Exception {
        String[] parts = identity.split("==", -1);
        if (parts.length != ID_PARTS) {
            throw new IllegalArgumentException("Expected name==version: " + identity);
        }
        Map<?, ?> info = Json.object(fetch("/pypi/" + parts[0] + "/" + parts[1] + "/json").get("info"));
        String version = String.valueOf(info.get("version"));
        if (!version.equals(parts[1])) {
            throw new IllegalArgumentException("Repository returned a different package version");
        }
        if (!pythonCompatible(info.get("requires_python"))) {
            throw new IllegalArgumentException("Package requires another Python version: " + identity);
        }
        List<String> result = new ArrayList<>();
        for (Object declaration : Json.array(info.get("requires_dist"))) {
            Requirement requirement = Requirement.parse(String.valueOf(declaration));
            if (requirement != null) {
                result.add(resolve(requirement));
            }
        }
        return result.stream().distinct().toList();
    }

    private String resolve(Requirement requirement) throws Exception {
        String name = requirement.name();
        if (selected.containsKey(name)) {
            String version = selected.get(name);
            if (!Versions.matches(version, requirement.constraint())) {
                throw new IllegalArgumentException("Version conflict for " + name + ": " + requirement.constraint());
            }
            return name + "==" + version;
        }
        Map<?, ?> releases = Json.object(fetch("/pypi/" + name + "/json").get("releases"));
        List<String> candidates = releases.keySet().stream().map(Object::toString)
                .filter(Versions::stable).filter(item -> Versions.matches(item, requirement.constraint()))
                .sorted((left, right) -> Versions.compare(right, left)).toList();
        for (String version : candidates) {
            if (available(releases.get(version))) {
                selected.put(name, version);
                return name + "==" + version;
            }
        }
        throw new IllegalArgumentException("No supported release for " + name + requirement.constraint());
    }

    private boolean available(Object release) {
        for (Object file : Json.array(release)) {
            Map<?, ?> metadata = Json.object(file);
            if (!Boolean.TRUE.equals(metadata.get("yanked")) && pythonCompatible(metadata.get("requires_python"))) {
                return true;
            }
        }
        return false;
    }

    private boolean pythonCompatible(Object constraint) {
        return constraint == null || Versions.matches(Markers.PYTHON_VERSION, constraint.toString());
    }

    private Map<?, ?> fetch(String path) throws Exception {
        if (!cache.containsKey(path)) {
            URI uri = URI.create(base + path);
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(TIMEOUT)
                    .header("Accept", "application/json").GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("PyPI HTTP " + response.statusCode() + ": " + uri);
            }
            cache.put(path, Json.object(Json.parse(response.body())));
        }
        return cache.get(path);
    }
}
