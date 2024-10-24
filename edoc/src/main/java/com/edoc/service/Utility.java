package com.edoc.service;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public class Utility {

    public static final String RESPONSE = "response";
    public static final String STATUS_CODE = "status";
    public static final String MESSAGE = "message";
    public static final String USER_MESSAGE = "user message";
    public static final String DEVELOPER_MESSAGE = "developer message";
    public static final String ERROR_CODE = "error code";

    public static JSONObject getResponse(String key, Object value, HttpStatus httpStatus) {
        if (key == null) key = MESSAGE;
        JSONObject jsonObject = new JSONObject();
        JSONObject msgObject = new JSONObject();
        msgObject.put(key, value);
        jsonObject.put(RESPONSE, msgObject);
        jsonObject.put(STATUS_CODE, httpStatus);
        return jsonObject;
    }

    public static JSONObject getResponse(Object value, HttpStatus httpStatus) {
        return getResponse(null, value, httpStatus);
    }

    public static JSONObject getErrorResponse(String userMessage, String developerMessage, HttpStatus httpStatus) {
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> msgObject = new LinkedHashMap<>();
        msgObject.put(USER_MESSAGE, userMessage);
        msgObject.put(DEVELOPER_MESSAGE, developerMessage);
        msgObject.put(DEVELOPER_MESSAGE, developerMessage);
        msgObject.put(ERROR_CODE, httpStatus.value());
        jsonObject.put(RESPONSE, msgObject);
        jsonObject.put(STATUS_CODE, httpStatus);
        return jsonObject;
    }

    public static JSONObject getErrorResponse(String userMessage, HttpStatus httpStatus) {
        return getErrorResponse(userMessage, userMessage, httpStatus);
    }

    public static JSONObject NO_DATA_AVAILABLE() {
        return Utility.getErrorResponse("No document found", HttpStatus.NOT_FOUND);
    }

    public static String generateId() {
        return "EDOC-" + UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 6)
                .toUpperCase()
                + "-" + System.currentTimeMillis();
    }

    public static ResponseEntity<?> generateResponse(JSONObject object) {
        return ResponseEntity.status((HttpStatusCode) object.get(STATUS_CODE)).body(object.get(RESPONSE));
    }

}
