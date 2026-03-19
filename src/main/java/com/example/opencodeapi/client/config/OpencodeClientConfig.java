package com.example.opencodeapi.client.config;

import lombok.Getter;

@Getter
public class OpencodeClientConfig {

    private final String baseUrl;
    private final String username;
    private final String password;
    private final long timeoutMs;
    private final long blockingTimeoutMs;

    public OpencodeClientConfig() {
        this(System.getenv("OPENCODE_BASE_URL"),
                System.getenv("OPENCODE_USERNAME"),
                System.getenv("OPENCODE_PASSWORD"),
                System.getenv("OPENCODE_TIMEOUT_MS"),
                System.getenv("OPENCODE_BLOCKING_TIMEOUT_MS"));
    }

    public OpencodeClientConfig(String baseUrl, String username, String password,
                                String timeoutMs, String blockingTimeoutMs) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "OPENCODE_BASE_URL is required. "
                            + "Set it to your opencode server address, e.g. http://127.0.0.1:4096");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = (username == null || username.isBlank()) ? "opencode" : username;
        this.password = password;
        this.timeoutMs = parseLong(timeoutMs, 10_000L);
        this.blockingTimeoutMs = parseLong(blockingTimeoutMs, 3_000L);
    }

    public boolean hasAuth() {
        return password != null && !password.isBlank();
    }

    private static long parseLong(String value, long defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
