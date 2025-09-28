package utec.apitester;

public class StepExecutor {
    private final String baseUrl;

    public StepExecutor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StepResponse execute(Step step) throws Exception {
        var caller = new HttpCaller(baseUrl);

        var stepResponse = new StepResponse();
        stepResponse.setSuccess();

        // NOTE: request will have at least one body (GET will have it empty)
        for (String body : step.getRequest().getBodies()) {
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
                try {
                    step.getExpected().validator().accept(stepResponse.getResponseJSON());
                } catch (Exception e) {
                    stepResponse.setException(e);
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
