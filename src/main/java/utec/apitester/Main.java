package utec.apitester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utec.apitester.utils.HttpCaller;
import utec.apitester.utils.PrintUtils;

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
        logger.info("🧹 Cleaning up");
        HttpCaller caller = new HttpCaller(baseUrl);
        caller.httpAny("DELETE", "/cleanup", "");
        logger.info("🧹 Cleaned");

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
                PrintUtils.groupSkipped(stepGroup.getName());
                continue;
            } else {
                System.out.println();
                PrintUtils.groupStart(stepGroup.getName());
            }

            totalGroups++;

            // begin steps
            for (Map.Entry<String, Step> entryStep : stepGroup.getSteps().entrySet()) {
                var step = entryStep.getValue();

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
                    PrintUtils.stepResult(stepGroup, step, stepResponse);
                }
            }
            // end steps

            var groupScore = stepGroup.getScore();
            PrintUtils.groupEnd(groupSuccess, groupFailure, groupScore);
            if (groupFailure == 0) {
                finalScore += groupScore;
            }

            totalSuccess += groupSuccess;
            totalFailure += groupFailure;

            if (this.stepped) {
                System.out.println("⌨️ (Stepped Mode) Press Enter to continue ...");
                System.in.read();
            }
        }

        // special case for email
        if (this.includeNiceToHave) {
            System.out.println();
            PrintUtils.groupStart("Special Case: Review Email Notification");
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

                // force all results to show
                var results = Stream.of("bookingDate",
                                        "customerFirstName",
                                        "customerLastName",
                                        "flightNumber",
                                        "estDepartureTime",
                                        "estArrivalTime"
                ).map(f -> {
                    var value = bookingInfo.getString(f);
                    if (content.contains(value)) {
                        System.out.printf("➕ Found %s: %s\n", f, value);
                        return true;
                    } else {
                        System.out.printf("➖ Not Found %s: %s\n", f, value);
                        return false;
                    }
                }).toList();

                // reduce it
                success = results.stream().allMatch(b -> b);

                if (!success) {
                    reason = "Some fields were not found in the email content";
                }
            }

            System.out.println();
            double groupScore = 0.2;
            PrintUtils.groupEnd(success ? 1 : 0, !success ? 1 : 0, groupScore);
            if (success) {
                totalSuccess++;
                finalScore += groupScore;
            } else {
                totalFailure++;
            }
        }

        PrintUtils.grandTotal(totalGroups, totalSuccess, totalFailure, finalScore);
    }
}
