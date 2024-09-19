package com.edoc.service;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class Utility {

    public static final String RESPONSE = "response";
    public static final String STATUS_CODE = "status";

    public static JSONObject getResponse(String key, Object value, HttpStatusCode statusCode) {
        key = (key.isEmpty() || key.isBlank()) ? "message" : key;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("response", new JSONObject().put(key, value));
        jsonObject.put(STATUS_CODE, statusCode);
        return jsonObject;
    }

    public static JSONObject getErrorResponse(String message, HttpStatusCode statusCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RESPONSE, message);
        jsonObject.put(STATUS_CODE, statusCode);
        return jsonObject;
    }

    public static JSONObject NO_DATA_AVAILABLE() {
        return Utility.getErrorResponse("No document found", HttpStatus.NOT_FOUND);
    }

}
