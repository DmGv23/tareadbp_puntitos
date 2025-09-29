package utec.apitester.utils;

import utec.apitester.Step;
import utec.apitester.StepGroup;
import utec.apitester.StepResponse;

public class PrintUtils {
    public static void groupSkipped(String name) {
        System.out.printf("🫥 (Skipped) Group: %s\n", name);
    }

    public static void groupStart(String name) {
        System.out.println("====================================");
        System.out.printf("🗂️ Group: %s\n", name);
        System.out.println();
    }

    public static void groupEnd(int successes, int failures, double score) {
        System.out.println();
        if (failures == 0) {
            System.out.printf("✅ Group Succeeded: %d of %d\n", successes, successes + failures);
            System.out.printf("🎉 POINTS WON: %.2f\n", score);
        } else {
            System.out.printf("❌ Group Succeeded: %d of %d\n", successes, successes + failures);
            System.out.println("😞 NO POINTS WON");
        }
    }

    public static void stepSkipped(String stepFullName) {
        System.out.printf("🫥 (Skipped) Step: %s\n", stepFullName);
    }

    public static void stepResult(StepGroup stepGroup, Step step, StepResponse stepResponse) {
        System.out.printf("""
                                  ------------------------------------
                                  📌 Step: %s
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
                          stepResponse.isSuccess() ? "➕ SUCCESS" : "➖ FAILURE ->\n" + stepResponse.getException()
                                                                                                  .getMessage()
        );
    }

    public static void grandTotal(int totalGroups, int totalSuccesses, int totalFailures, double finalScore) {
        System.out.println();
        System.out.println();
        System.out.println("====================================");
        System.out.println("========== GRAND TOTAL =============");
        System.out.println("====================================");
        System.out.printf("  🗂️ Total Groups: %d\n", totalGroups);

        String icon = totalFailures == 0 ? "✅" : "❌";
        System.out.printf("  %s Total Succeeded: %d of %d\n", icon, totalSuccesses, totalSuccesses + totalFailures);
        System.out.printf("  🧮 FINAL SCORE: %.2f\n", finalScore);
        System.out.println();
        System.out.println(" (Must-Have Max Score = 1.5)");
        System.out.println(" (Nice-To-Have Max Score = 1)");
        System.out.println("====================================");
        System.out.println();
    }
}
