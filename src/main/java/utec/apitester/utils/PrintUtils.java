package utec.apitester.utils;

import utec.apitester.Step;
import utec.apitester.StepGroup;
import utec.apitester.StepResponse;

public class PrintUtils {
    public static void groupSkipped(String name) {
        System.out.printf("🫥 (Skipped) Group: %s\n", name);
    }

    public static void groupStart(String name, Double score) {
        System.out.println("====================================");
        System.out.printf("🗂️ Group: %s (%f)\n", name, score);
        System.out.println();
    }

    public static void groupEnd(int successes, int failures, double score) {
        System.out.println();
        if (failures == 0) {
            System.out.printf("✅ Group Succeeded: %d of %d\n", successes, successes + failures);
            System.out.printf("🎉 POINTS WON: %.2f\n", score);
        } else {
            System.out.printf("❌ Group Succeeded: %d of %d\n", successes, successes + failures);
            System.out.println("😞 POINTS WON: 0.0");
        }
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
                          step.getRequest() != null ? step.getRequest().getMethod() : "CUSTOM",
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

    public static void grandTotal(int totalMustHaveGroups, int totalNiceToHaveGroups, int totalMustHaveSuccess,
                                  int totalMustHaveFail, int totalNiceHaveSuccess, int totalNiceHaveFail,
                                  double totalMustHaveScore, double totalNiceHaveScore, double maxMustHaveScore,
                                  double maxNiceHaveScore) {
        System.out.println();
        System.out.println();
        System.out.println("====================================");
        System.out.println("========== GRAND TOTAL =============");
        System.out.println("====================================");
        System.out.printf("  🗂️ Must Have Groups: %d\n", totalMustHaveGroups);
        System.out.printf("  🗂️ Nice To Have Groups: %d\n", totalNiceToHaveGroups);
        System.out.printf("  🗂️ Total Groups: %d\n", totalMustHaveGroups + totalNiceToHaveGroups);

        String iconSuccess = "✅";
        String iconFail = "❌";
        System.out.printf("  %s Must Have Succeeded: %d of %d\n",
                          totalMustHaveFail == 0 ? iconSuccess : iconFail,
                          totalMustHaveSuccess,
                          totalMustHaveSuccess + totalMustHaveFail
        );
        System.out.printf("  %s Nice To Have Succeeded: %d of %d\n",
                          totalNiceHaveFail == 0 ? iconSuccess : iconFail,
                          totalNiceHaveSuccess,
                          totalNiceHaveSuccess + totalNiceHaveFail
        );
        System.out.printf("  %s Total Succeeded: %d of %d\n",
                          totalMustHaveFail + totalNiceHaveFail == 0 ? iconSuccess : iconFail,
                          totalMustHaveSuccess + totalNiceHaveSuccess,
                          totalMustHaveSuccess + totalMustHaveFail + totalNiceHaveSuccess + totalNiceHaveFail
        );
        System.out.printf("  🧮 Must Have Score: %.2f of %.2f\n", totalMustHaveScore, maxMustHaveScore);
        System.out.printf("  🧮 Nice To Have Score: %.2f of %.2f\n", totalNiceHaveScore, maxNiceHaveScore);
        System.out.printf("  🧮 FINAL SCORE: %.2f\n", totalMustHaveScore + totalNiceHaveScore);
        System.out.println();
        System.out.println("====================================");
        System.out.println();
    }
}
