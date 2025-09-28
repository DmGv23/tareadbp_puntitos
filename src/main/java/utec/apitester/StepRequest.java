package utec.apitester;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class StepRequest {
    private final String method;
    private final String path;
    private final List<String> bodies;
    private Function<HashMap<String, StepResponse>, JSONObject> bodyFunction;

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

    public StepRequest(String method, String path, Function<HashMap<String, StepResponse>, JSONObject> bodyFunction) {
        this(method, path, "");
        this.bodyFunction = bodyFunction;
    }

    public String getPath() {
        return path;
    }

    public List<String> getBodies() {
        return bodies;
    }

    public String getMethod() {
        return method;
    }
}
