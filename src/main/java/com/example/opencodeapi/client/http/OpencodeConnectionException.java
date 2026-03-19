package com.example.opencodeapi.client.http;

import lombok.Getter;

@Getter
public class OpencodeConnectionException extends RuntimeException {

    private final String path;

    public OpencodeConnectionException(String path, Throwable cause) {
        super("Cannot connect to opencode server for path '%s'. "
                .formatted(path)
                + "Please ensure 'opencode serve' is running and OPENCODE_BASE_URL is set correctly.",
                cause);
        this.path = path;
    }
}
