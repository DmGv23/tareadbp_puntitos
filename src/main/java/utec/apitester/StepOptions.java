package utec.apitester;

public record StepOptions(Boolean mustHave, Boolean isProtected, Boolean reportSuccess, Boolean saveResponse) {
    public StepOptions(Boolean mustHave, Boolean isProtected, Boolean reportSuccess) {
        this(mustHave, isProtected, reportSuccess, false);
    }
}
