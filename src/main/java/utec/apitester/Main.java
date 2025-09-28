package utec.apitester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private final Logger logger = LoggerFactory.getLogger(Main.class);
    private final String baseUrl;
    private final Boolean stepped;
    private final Boolean includeNiceToHave;
    private final HashMap<String, StepGroup> stepGroups;
    private final HashMap<String, StepResponse> responses;

    public Main(String baseUrl, Boolean stepped, Boolean includeNiceToHave) {
        this.baseUrl = baseUrl;
        this.stepped = stepped;
        this.includeNiceToHave = includeNiceToHave;
        this.stepGroups = new StepsInitializer().initialize();
        this.responses = new HashMap<>();
    }

    public void start() throws Exception {
        this.responses.clear();
        HttpCaller caller = new HttpCaller(baseUrl);
        caller.httpAny("DELETE", "/cleanup", "");

        var executor = new StepExecutor(baseUrl);
        Double finalScore = 0D;
        for (Map.Entry<String, StepGroup> entryGroup : this.stepGroups.entrySet()) {
            var stepGroup = entryGroup.getValue();

            var canRunGroup = stepGroup.isMustHave() || this.includeNiceToHave;
            if (!canRunGroup) {
                System.out.printf("(Skipped) Group: %s\n", stepGroup.getName());
                continue;
            }

            var anyFailure = false;
            for (Map.Entry<String, Step> entryStep : stepGroup.getSteps().entrySet()) {
                var step = entryStep.getValue();

                var canRunStep = step.getOptions().mustHave() || this.includeNiceToHave;
                if (!canRunStep) {
                    System.out.printf("(Skipped) Step: %s\n", stepGroup.getStepFullTitle(step));
                    continue;
                }

                var stepResponse = executor.execute(step);
                if (!anyFailure && !stepResponse.isSuccess()) {
                    anyFailure = true;
                }

                if (!stepResponse.isSuccess() || (stepResponse.isSuccess() && (step.getOptions()
                                                                                   .reportSuccess() || logger.isDebugEnabled()))) {
                    reportResponse(stepGroup, step, stepResponse);
                }

                // TODO: should responses be grouped too?
                this.responses.put(step.getName(), stepResponse);
            }

            if (!anyFailure) {
                var groupScore = stepGroup.getScore();
                finalScore += groupScore;
                System.out.printf("SCORE WON: %f\n", groupScore);
            } else {
                System.out.println("SCORE WON: 0.0 (One of the tests failed)");
            }

            if (this.stepped) {
                System.out.println("(Stepped Mode) Press any key to continue ...");
                System.in.read();
            }
        }

        System.out.printf("FINAL SCORE: %f\n", finalScore);
    }

    private void reportResponse(StepGroup stepGroup, Step step, StepResponse stepResponse) {
        var responseReceived = "";

        if (stepResponse.isSuccess()) {
            if (stepResponse.getResponseJSON() != null) {
                responseReceived = stepResponse.getResponseJSON().toString(2);
            } else {
                responseReceived = stepResponse.getResponseString();
            }
        } else {
            responseReceived = stepResponse.getException().getMessage();
        }

        System.out.printf("""
                                  ------------------------------------
                                  Step: %s
                                  Description: %s
                                  Request:
                                  %s %s
                                  %s
                                  
                                  Response Received:
                                  %s %s
                                  
                                  Result: %s
                                  """,
                          stepGroup.getStepFullTitle(step),
                          step.getDescription(),
                          step.getRequest().getMethod(),
                          step.getRequest().getPath(),
                          // show the last request sent
                          stepResponse.getRequestBody(),
                          // show the last response received
                          stepResponse.getResponseStatus(),
                          responseReceived,
                          stepResponse.isSuccess() ? "SUCCESS" : "FAILURE"
        );
    }
}
