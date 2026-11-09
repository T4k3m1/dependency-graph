package depgraph;

import java.util.List;


public interface Repository {

    List<String> dependencies(String identity) throws Exception;
}
