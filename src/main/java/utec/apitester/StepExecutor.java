package utec.apitester;

public class StepExecutor {
    private final String baseUrl;

    public StepExecutor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StepResponse execute(Step step) throws Exception {
        var caller = new HttpCaller(baseUrl);
        var httpResponse = caller.httpAny(step.getRequest().method(),
                                          step.getRequest().path(),
                                          step.getRequest().body()
        );

        var stepResponse = new StepResponse();
        stepResponse.setSuccess();
        stepResponse.setResponseString(httpResponse.body());

        if (step.getExpected().httpStatus() != httpResponse.statusCode()) {
            stepResponse.setError(new HttpStatusMismatchException(step.getExpected().httpStatus(),
                                                                  httpResponse.statusCode()
            ));
        } else if (stepResponse.getResponseJSON() != null && step.getExpected().validator() != null) {
            try {
                step.getExpected().validator().accept(stepResponse.getResponseJSON());
            } catch (Exception e) {
                stepResponse.setError(e);
            }
        }

        return stepResponse;
    }
}
