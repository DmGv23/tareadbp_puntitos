package utec.apitester;

import org.json.JSONException;
import org.json.JSONObject;

public class StepResponse {
    private Boolean success = false;
    private Exception error;
    private JSONObject responseJSON;
    private String responseString;

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess() {
        this.success = true;
        this.error = null;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.success = false;
        this.error = error;
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
            this.responseJSON = null;
        }
    }
}
