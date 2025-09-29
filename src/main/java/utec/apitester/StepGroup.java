package utec.apitester;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class StepGroup {
    private final String name;
    private final Double score;
    private final Boolean mustHave;
    private final HashMap<String, Step> steps;

    public StepGroup(String name, Double score, Boolean mustHave) {
        this.name = name;
        this.score = score;
        this.mustHave = mustHave;
        steps = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public Double getScore() {
        return score;
    }

    public void addStep(Step step) {
        steps.put(step.getName(), step);
    }

    public HashMap<String, Step> getSteps() {
        return steps;
    }

    public void clear() {
        steps.clear();
    }

    public Boolean isMustHave() {
        return mustHave;
    }

    public String getStepFullTitle(Step step) {
        return String.format("[%s/%s] %s",
                             this.getName(),
                             this.isMustHave() ? "MUST-HAVE" : "NICE-TO-HAVE",
                             step.getName()
        );
    }
}
