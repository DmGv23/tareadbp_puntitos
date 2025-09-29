package utec.apitester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utec.apitester.utils.HttpCaller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Main {
    private final Logger logger = LoggerFactory.getLogger(Main.class);
    private final String baseUrl;
    private final Boolean stepped;
    private final Boolean includeNiceToHave;
    private final HashMap<String, StepGroup> stepGroups;

    public Main(String baseUrl, Boolean stepped, Boolean includeNiceToHave) {
        this.baseUrl = baseUrl;
        this.stepped = stepped;
        this.includeNiceToHave = includeNiceToHave;
        this.stepGroups = new StepsInitializer().initialize();
    }

    public void start() throws Exception {
        logger.info("Cleaning up");
        HttpCaller caller = new HttpCaller(baseUrl);
        caller.httpAny("DELETE", "/cleanup", "");
        logger.info("Cleaned");

        int totalGroups = 0;
        int totalSuccess = 0;
        int totalFailure = 0;
        var executor = new StepExecutor(baseUrl);
        Double finalScore = 0D;
        var responses = new HashMap<String, StepResponse>();
        for (Map.Entry<String, StepGroup> entryGroup : this.stepGroups.entrySet()) {
            int groupSuccess = 0;
            int groupFailure = 0;
            var stepGroup = entryGroup.getValue();

            var canRunGroup = stepGroup.isMustHave() || this.includeNiceToHave;
            if (!canRunGroup) {
                System.out.printf("(Skipped) Group: %s\n", stepGroup.getName());
                continue;
            } else {
                System.out.println();
                System.out.println("====================================");
                System.out.printf("Group: %s\n", stepGroup.getName());
            }

            totalGroups++;

            // begin steps
            for (Map.Entry<String, Step> entryStep : stepGroup.getSteps().entrySet()) {
                var step = entryStep.getValue();

                var canRunStep = step.getOptions().mustHave() || this.includeNiceToHave;
                if (!canRunStep) {
                    System.out.printf("(Skipped) Step: %s\n", stepGroup.getStepFullTitle(step));
                    continue;
                }

                var stepResponse = executor.execute(step, responses);

                if (!stepResponse.isSuccess()) {
                    groupFailure++;
                } else {
                    groupSuccess++;

                    if (step.getOptions().saveResponse()) {
                        responses.put(step.getName(), stepResponse);
                    }
                }

                // failures are always reported
                // successes are reported if configured or debug
                if (!stepResponse.isSuccess() || (stepResponse.isSuccess() && (step.getOptions()
                                                                                   .reportSuccess() || logger.isDebugEnabled()))) {
                    reportResponse(stepGroup, step, stepResponse);
                }
            }
            // end steps

            System.out.println();
            System.out.printf("Group Succeeded: %d of %d\n", groupSuccess, groupSuccess + groupFailure);
            if (groupFailure == 0) {
                var groupScore = stepGroup.getScore();
                finalScore += groupScore;
                System.out.printf("POINTS WON: %f\n", groupScore);
            } else {
                System.out.println("POINTS WON: 0.0");
            }

            totalSuccess += groupSuccess;
            totalFailure += groupFailure;

            if (this.stepped) {
                System.out.println("(Stepped Mode) Press Enter to continue ...");
                System.in.read();
            }
        }

        // special case for email
        if (this.includeNiceToHave) {
            System.out.println();
            System.out.println("====================================");
            System.out.println("Special Case: Review Email Notification");
            System.out.println();
            totalGroups++;

            var bookingInfo = responses.get("READ_SUCCESS_BOOK_FLIGHT_AA448").getResponseJSON();

            var emailPath = Paths.get(String.format("flight_booking_email_%s.txt", bookingInfo.getString("id")));
            System.out.printf("Expected Path: %s\n", emailPath.toAbsolutePath());

            boolean success = true;
            String reason = "";
            if (!Files.exists(emailPath)) {
                reason = "File not found";
                success = false;
            } else {
                String content = Files.readString(emailPath);

                success = Stream.of("bookingDate",
                                    "customerFirstName",
                                    "customerLastName",
                                    "flightNumber",
                                    "estDepartureTime",
                                    "estArrivalTime"
                ).allMatch(f -> {
                    var value = bookingInfo.getString(f);
                    if (content.contains(value)) {
                        System.out.printf("✔️ Found %s: %s\n", f, value);
                        return true;
                    } else {
                        System.out.printf("❌️ Not Found %s: %s\n", f, value);
                        return false;
                    }
                });

                if (!success) {
                    reason = "Some fields were not found in the email content";
                }
            }

            System.out.println();
            if (success) {
                double score = 0.2;
                totalSuccess++;
                finalScore += score;
                System.out.println("Result: SUCCESS");
                System.out.printf("POINTS WON: %f\n", score);
            } else {
                totalFailure++;
                System.out.printf("Result: FAILURE (%s)\n", reason);
                System.out.println("POINTS WON: 0.0");
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("====================================");
        System.out.println("========== GRAND TOTAL =============");
        System.out.println("====================================");
        System.out.printf("  Total Groups: %d\n", totalGroups);
        System.out.printf("  Total Succeeded: %d of %d\n", totalSuccess, totalSuccess + totalFailure);
        System.out.printf("  FINAL SCORE: %f\n", finalScore);
        System.out.println();
        System.out.println(" (Must-Have Max Score = 1.5)");
        System.out.println(" (Nice-To-Have Max Score = 1)");
        System.out.println("====================================");
        System.out.println();
    }

    private void reportResponse(StepGroup stepGroup, Step step, StepResponse stepResponse) {
        System.out.printf("""
                                  ------------------------------------
                                  Step: %s
                                  Description: %s
                                  Request: %s %s
                                    %s
                                  Response Received:
                                    %s
                                    %s
                                  
                                  Result: %s
                                  
                                  """,
                          stepGroup.getStepFullTitle(step),
                          step.getDescription(),
                          step.getRequest().getMethod(),
                          stepResponse.getRequestPath(),
                          // show the last request sent
                          stepResponse.getRequestBody(),
                          // show the last response received
                          stepResponse.getResponseStatus(),
                          stepResponse.getResponseJSON() != null ? stepResponse.getResponseJSON()
                                                                               .toString(2) : stepResponse.getResponseString(),
                          stepResponse.isSuccess() ? "SUCCESS" : "FAILURE ->\n" + stepResponse.getException()
                                                                                              .getMessage()
        );
    }
}
