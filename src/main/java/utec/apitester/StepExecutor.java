package utec.apitester;

import utec.apitester.utils.HttpCaller;

import java.util.HashMap;

public class StepExecutor {
    private final String baseUrl;

    public StepExecutor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StepResponse execute(Step step, HashMap<String, StepResponse> responses) throws Exception {
        var caller = new HttpCaller(baseUrl);

        var stepResponse = new StepResponse();
        stepResponse.setSuccess();

        // NOTE: request will have at least one body (GET will have it empty)
        for (String body : step.getRequest().getBodies()) {
            if (step.getOptions().isProtected()) {
                var loginResponse = responses.get("LOGIN_SUCCESS");
                var token = loginResponse.getResponseJSON().getString("token");
                caller.setBearerToken(token);
            }

            var httpResponse = caller.httpAny(step.getRequest().getMethod(), step.getRequest().getPath(), body);

            // write the last information executed
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
