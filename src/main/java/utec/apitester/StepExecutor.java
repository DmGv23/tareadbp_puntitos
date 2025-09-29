package utec.apitester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utec.apitester.utils.HttpCaller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StepExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StepExecutor.class);
    private final String baseUrl;

    public StepExecutor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StepResponse execute(Step step, HashMap<String, StepResponse> responses) throws Exception {
        var caller = new HttpCaller(baseUrl);

        var stepResponse = new StepResponse();
        stepResponse.setSuccess();

        String requestPath;
        if (step.getRequest().getPathFunction() != null) {
            requestPath = step.getRequest().getPathFunction().apply(responses);
        } else {
            requestPath = step.getRequest().getPath();
        }

        List<String> bodies;
        if (step.getRequest().getBodyFunction() != null) {
            var jo = step.getRequest().getBodyFunction().apply(responses);
            bodies = Arrays.asList(jo.toString());
        } else {
            bodies = step.getRequest().getBodies();
        }

        // NOTE: request will have at least one body (GET will have it empty)
        for (String body : bodies) {
            if (step.getOptions().isProtected()) {
                var loginResponse = responses.get("LOGIN_SUCCESS");
                var token = loginResponse.getResponseJSON().getString("token");
                caller.setBearerToken(token);
            }

            if (step.getOptions().preWaitSeconds() > 0) {
                logger.info("Waiting for {} seconds", step.getOptions().preWaitSeconds());
                Thread.sleep(step.getOptions().preWaitSeconds() * 1000);
            }

            var httpResponse = caller.httpAny(step.getRequest().getMethod(), requestPath, body);

            // write the last information executed
            stepResponse.setRequestPath(requestPath);
            stepResponse.setRequestBody(body);
            stepResponse.setResponseString(httpResponse.body());
            stepResponse.setResponseStatus(httpResponse.statusCode());

            // on first error, break
            if (step.getExpected().httpStatus() != httpResponse.statusCode()) {
                stepResponse.setException(new HttpStatusMismatchException(step.getExpected().httpStatus(),
                                                                          httpResponse.statusCode()
                ));
            } else if (stepResponse.getResponseJSON() != null && step.getExpected().validator() != null) {
                var exception = step.getExpected().validator().apply(stepResponse.getResponseJSON());
                if (exception != null) {
                    stepResponse.setException(exception);
                    break;
                }
            }

            if (!stepResponse.isSuccess()) {
                break;
            }
        }

        return stepResponse;
    }
}
