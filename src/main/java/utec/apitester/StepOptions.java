package utec.apitester;

public record StepOptions(Boolean mustHave, Boolean isProtected, Boolean reportSuccess, Boolean saveResponse,
                          Integer preWaitSeconds) {
    public StepOptions(Boolean mustHave, Boolean isProtected, Boolean reportSuccess) {
        this(mustHave, isProtected, reportSuccess, false, 0);
    }

    public StepOptions(Boolean mustHave, Boolean isProtected, Boolean reportSuccess, Boolean saveResponse) {
        this(mustHave, isProtected, reportSuccess, saveResponse, 0);
    }
}
