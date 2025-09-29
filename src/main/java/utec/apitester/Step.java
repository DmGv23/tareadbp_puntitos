package utec.apitester;

public class Step {
    private String name;
    private StepRequest request;
    private StepOptions options;
    private StepExpected expected;
    private String description;

    public static Step create(String name, String description, StepRequest request, StepOptions options,
                              StepExpected expected) {
        var step = new Step();
        step.description = description;
        step.name = name;
        step.request = request;
        step.options = options;
        step.expected = expected;
        return step;
    }

    public String getName() {
        return name;
    }

    public StepRequest getRequest() {
        return request;
    }

    public StepOptions getOptions() {
        return options;
    }

    public StepExpected getExpected() {
        return expected;
    }

    public String getDescription() {
        return description;
    }
}