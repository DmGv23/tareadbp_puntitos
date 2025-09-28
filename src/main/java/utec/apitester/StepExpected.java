package utec.apitester;

import org.json.JSONObject;

import java.util.function.Consumer;

public record StepExpected(int httpStatus, Consumer<JSONObject> validator) {
    public StepExpected(int httpStatus) {
        this(httpStatus, null);
    }
}
