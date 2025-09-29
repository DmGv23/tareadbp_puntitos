package utec.apitester;

public record StepOptions(Boolean isProtected, Boolean reportSuccess, Boolean saveResponse, Integer preWaitSeconds) {
    public StepOptions(Boolean isProtected, Boolean reportSuccess) {
        this(isProtected, reportSuccess, false, 0);
    }

    public StepOptions(Boolean isProtected, Boolean reportSuccess, Boolean saveResponse) {
        this(isProtected, reportSuccess, saveResponse, 0);
    }
}
