package depgraph;

import java.util.List;

/**
 * Provides direct package dependencies without invoking a package manager.
 * Implementations may read local test data or retrieve metadata from a remote
 * package repository.
 */
public interface Repository {
    /**
     * Returns the direct dependencies of the requested package identity.
     *
     * @param identity package identity in {@code name==version} form
     * @return fixed-version identities of direct dependencies; never {@code null}
     * @throws Exception if dependency metadata cannot be retrieved or parsed
     */
    List<String> dependencies(String identity) throws Exception;
}
