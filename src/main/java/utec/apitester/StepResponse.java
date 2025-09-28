package utec.apitester;

import org.json.JSONException;
import org.json.JSONObject;

public class StepResponse {
    private Boolean success = false;
    private Exception exception;
    private String requestBody;
    private JSONObject responseJSON;
    private String responseString;
    private Integer responseStatus;
    private String requestPath;

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess() {
        this.success = true;
        this.exception = null;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.success = false;
        this.exception = exception;
    }

    public JSONObject getResponseJSON() {
        return responseJSON;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;

        try {
            this.responseJSON = new JSONObject(responseString);
        } catch (JSONException ex) {
            // ignore any json parsing error, and simply assume the response was a string
            this.responseJSON = null;
        }
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
}
