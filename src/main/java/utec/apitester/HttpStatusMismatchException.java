package utec.apitester;

public class HttpStatusMismatchException extends Exception {
    public HttpStatusMismatchException(int expected, int actual) {
        super(String.format("HTTP Status Mismatch. Expected: %s, Actual: %s.", expected, actual));
    }
}
