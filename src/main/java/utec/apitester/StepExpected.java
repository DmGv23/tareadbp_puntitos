package utec.apitester;

import org.json.JSONObject;

import java.util.function.Function;

public record StepExpected(int httpStatus, Function<JSONObject, Exception> validator) {
    public StepExpected(int httpStatus) {
        this(httpStatus, null);
    }
}
