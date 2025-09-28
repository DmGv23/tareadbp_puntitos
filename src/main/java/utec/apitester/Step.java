package utec.apitester;

import java.util.function.Function;

public class Step {
    private String name;
    private StepRequest request;
    private StepOptions options;
    private StepExpected expected;
    private Function<Step, String> description;
    private Function<Step, String> title;

    public static Step create(String name, String title, String description, StepRequest request, StepOptions options,
                              StepExpected expected) {
        return Step.create(name, (Step s) -> title, (Step s) -> description, request, options, expected);
    }

    public static Step create(String name, String title, Function<Step, String> description, StepRequest request,
                              StepOptions options, StepExpected expected) {
        return Step.create(name, (Step s) -> title, description, request, options, expected);
    }

    public static Step create(String name, Function<Step, String> title, String description, StepRequest request,
                              StepOptions options, StepExpected expected) {
        return Step.create(name, title, (Step s) -> description, request, options, expected);
    }

    public static Step create(String name, Function<Step, String> title, Function<Step, String> description,
                              StepRequest request, StepOptions options, StepExpected expected) {
        var step = new Step();
        step.title = title;
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

    public Function<Step, String> getDescription() {
        return description;
    }

    public Function<Step, String> getTitle() {
        return title;
    }
}