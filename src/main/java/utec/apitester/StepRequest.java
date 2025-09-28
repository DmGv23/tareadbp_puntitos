package utec.apitester;

import java.util.Arrays;
import java.util.List;

public class StepRequest {
    private final String method;
    private final String path;
    private final List<String> bodies;

    public StepRequest(String method, String path) {
        // this means it will always have at least one item
        this(method, path, "");
    }

    public StepRequest(String method, String path, String body) {
        this(method, path, Arrays.asList(body));
    }

    public StepRequest(String method, String path, List<String> bodies) {
        this.method = method;
        this.path = path;

        if (bodies.isEmpty()) {
            throw new IllegalArgumentException("Bodies should not be empty");
        }

        this.bodies = bodies;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return bodies.getFirst();
    }

    public List<String> getBodies() {
        return bodies;
    }

    public String getMethod() {
        return method;
    }
}
