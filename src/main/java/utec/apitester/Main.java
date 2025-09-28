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
    private final HashMap<String, Step> steps;
    private final HashMap<String, StepResponse> responses;

    public Main(String baseUrl, Boolean stepped, Boolean includeNiceToHave) {
        this.baseUrl = baseUrl;
        this.stepped = stepped;
        this.includeNiceToHave = includeNiceToHave;
        this.steps = new StepsInitializer().initialize();
        this.responses = new HashMap<>();
    }

    public void start() throws Exception {
        this.responses.clear();

        var executor = new StepExecutor(baseUrl);
        var index = 0;
        for (Map.Entry<String, Step> entry : this.steps.entrySet()) {
            var step = entry.getValue();
            var canRun = step.getOptions().mustHave() || this.includeNiceToHave;
            if (!canRun) {
                continue;
            }

            var stepResponse = executor.execute(step);
            if (!stepResponse.isSuccess() || (stepResponse.isSuccess() && (step.getOptions()
                                                                               .reportSuccess() || logger.isDebugEnabled()))) {
                reportResponse(step, stepResponse);
            }

            this.responses.put(step.getName(), stepResponse);

            // do not wait for the last step
            if (this.stepped && index != this.steps.size() - 1) {
                System.out.println("(Stepped Mode) Press any key to continue ...");
                System.in.read();
            }

            index++;
        }
    }

    private void reportResponse(Step step, StepResponse stepResponse) {
        var responseReceived = "";

        if (stepResponse.isSuccess()) {
            if (stepResponse.getResponseJSON() != null) {
                responseReceived = stepResponse.getResponseJSON().toString(2);
            } else {
                responseReceived = stepResponse.getResponseString();
            }
        } else {
            responseReceived = stepResponse.getError().getMessage();
        }

        System.out.printf("""
                                      Step: %s
                                      Description:
                                      %s
                                  
                                      Result: %s
                                      Response Received:
                                      %s
                                  """,
                          step.getTitle(),
                          step.getDescription(),
                          stepResponse.isSuccess() ? "SUCCESS" : "FAILURE",
                          responseReceived
        );
    }
}
