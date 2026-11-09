package depgraph;

import java.util.List;

/** Supplies direct dependencies without a package manager. */
public interface Repository {
    /** Returns fixed-version identities of direct dependencies. */
    List<String> dependencies(String identity) throws Exception;
}
