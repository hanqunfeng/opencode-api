package com.example.opencodeapi.client.http;

import lombok.Getter;

@Getter
public class OpencodeApiException extends RuntimeException {

    private final int statusCode;
    private final String path;
    private final String responseBody;

    public OpencodeApiException(int statusCode, String path, String responseBody) {
        super("API error %d on %s: %s".formatted(statusCode, path,
                responseBody != null && responseBody.length() > 200
                        ? responseBody.substring(0, 200) + "..."
                        : responseBody));
        this.statusCode = statusCode;
        this.path = path;
        this.responseBody = responseBody;
    }
}
