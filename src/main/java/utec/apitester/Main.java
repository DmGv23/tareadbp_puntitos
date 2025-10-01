package utec.apitester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utec.apitester.utils.HttpCaller;
import utec.apitester.utils.PrintUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

        var executor = new StepExecutor(baseUrl);
        int totalMustHaveGroups = 0;
        int totalNiceHaveGroups = 0;
        double theoreticalMaxMustHaveScore = 0;
        double theoreticalMaxNiceHaveScore = 0;
        var responses = new HashMap<String, StepResponse>();
        for (Map.Entry<String, StepGroup> entryGroup : this.stepGroups.entrySet()) {
            var stepGroup = entryGroup.getValue();
            if (stepGroup.isMustHave()) {
                theoreticalMaxMustHaveScore += stepGroup.getScore();
            } else {
                theoreticalMaxNiceHaveScore += stepGroup.getScore();
            }

            var canRunGroup = stepGroup.isMustHave() || this.includeNiceToHave;
            if (!canRunGroup) {
                PrintUtils.groupSkipped(stepGroup.getName());
                continue;
            } else {
                System.out.println();
                PrintUtils.groupStart(stepGroup.getName(), stepGroup.getScore());
            }

            if (stepGroup.isMustHave()) {
                totalMustHaveGroups++;
            } else {
                totalNiceHaveGroups++;
            }

            // begin steps
            for (Map.Entry<String, Step> entryStep : stepGroup.getSteps().entrySet()) {
                var step = entryStep.getValue();

                StepResponse stepResponse;
                if (!stepGroup.isCustom()) {
                    stepResponse = executor.execute(step, responses);
                    if (step.getName().equals("LOGIN_SUCCESS")) {
                        if (!stepResponse.isSuccess()) {
                            System.out.printf("💀 LOGIN_SUCCESS test failed (%s).\n Program will be terminated.",
                                              stepResponse.getException().getMessage()
                            );
                            System.exit(1);
                        }
                    }
                } else {
                    var bookingInfo = responses.get("READ_SUCCESS_BOOK_FLIGHT_AA448").getResponseJSON();

                    var emailPath = Paths.get(String.format("flight_booking_email_%s.txt",
                                                            bookingInfo.getString("id")
                    ));
                    System.out.printf("Expected Path: %s\n", emailPath.toAbsolutePath());
                    stepResponse = new StepResponse();
                    if (!Files.exists(emailPath)) {
                        stepResponse.setException(new Exception("File not found"));
                    } else {
                        String content = Files.readString(emailPath);

                        // force all results to show
                        String[] requiredFields = new String[]{"bookingDate", "customerFirstName", "customerLastName", "flightNumber", "estDepartureTime", "estArrivalTime"};

                        StringBuilder log = new StringBuilder();
                        var notFound = 0;
                        for (String f : requiredFields) {
                            var value = bookingInfo.getString(f);
                            if (content.contains(value)) {
                                log.append(String.format("➕ Found %s: %s\n", f, value));
                            } else {
                                notFound++;
                                log.append(String.format("➖ Not Found %s: %s\n", f, value));
                            }
                        }

                        if (notFound == 0) {
                            stepResponse.setSuccess();
                            stepResponse.setResponseString(log.toString());
                        } else {
                            stepResponse.setException(new Exception("Some fields were not found in the email content"));
                            stepResponse.setResponseString(log.toString());
                        }
                    }
                }

                stepGroup.countUpStep(stepResponse.isSuccess());
                if (stepResponse.isSuccess()) {
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

            var isGroupSuccess = stepGroup.getStepFailureCount() == 0;
            var groupScore = stepGroup.getScore();
            stepGroup.setGroupResult(isGroupSuccess);
            PrintUtils.groupEnd(stepGroup.getStepSuccessCount(), stepGroup.getStepFailureCount(), groupScore);

            if (this.stepped) {
                System.out.println("⌨️ (Stepped Mode) Press Enter to continue ...");
                System.in.read();
            }
        }

        var totalMustHaveSuccess = 0;
        var totalNiceHaveSuccess = 0;
        var totalMustHaveFail = 0;
        var totalNiceHaveFail = 0;
        var totalMustHaveScore = 0D;
        var totalNiceHaveScore = 0D;

        for (Map.Entry<String, StepGroup> entryGroup : stepGroups.entrySet()) {
            var stepGroup = entryGroup.getValue();
            if (stepGroup.getGroupResult()) {
                if (stepGroup.isMustHave()) {
                    totalMustHaveSuccess += stepGroup.getStepSuccessCount();
                    totalMustHaveScore += stepGroup.getScore();
                } else {
                    totalNiceHaveSuccess += stepGroup.getStepSuccessCount();
                    totalNiceHaveScore += stepGroup.getScore();
                }
            } else {
                if (stepGroup.isMustHave()) {
                    totalMustHaveFail += stepGroup.getStepFailureCount();
                } else {
                    totalNiceHaveFail += stepGroup.getStepFailureCount();
                }
            }
        }

        PrintUtils.grandTotal(totalMustHaveGroups, totalNiceHaveGroups,

                              totalMustHaveSuccess, totalMustHaveFail,

                              totalNiceHaveSuccess, totalNiceHaveFail,

                              totalMustHaveScore, totalNiceHaveScore,

                              theoreticalMaxMustHaveScore, theoreticalMaxNiceHaveScore
        );
    }
}
