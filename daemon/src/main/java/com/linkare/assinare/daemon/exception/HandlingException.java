package com.linkare.assinare.daemon.exception;

import org.json.JSONObject;

/**
 *
 * @author bnazare
 */
public class HandlingException extends Exception {
    
    private static final String KEY_MESSAGE = "message";
    
    private final JSONObject jsonObject;

    public HandlingException(Throwable cause) {
        super(cause);
        jsonObject = new JSONObject();
        jsonObject.put(KEY_MESSAGE, cause.getMessage());
    }
    
    public HandlingException(Throwable cause, JSONObject jsonObject) {
        super(cause);
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
    
}
