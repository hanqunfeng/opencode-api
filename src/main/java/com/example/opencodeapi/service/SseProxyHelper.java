package com.example.opencodeapi.service;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.http.OpencodeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SseProxyHelper {

    private final OpencodeClientConfig config;
    private final ExecutorService sseExecutor;
    private final long defaultTimeoutMs;

    public SseProxyHelper(OpencodeClientConfig config, ExecutorService sseExecutor, long defaultTimeoutMs) {
        this.config = config;
        this.sseExecutor = sseExecutor;
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    public long getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public long resolveTimeout(Long timeout) {
        if (timeout == null) {
            return defaultTimeoutMs;
        }
        if (timeout < 1000 || timeout > 300_000) {
            log.warn("SSE timeout {} ms out of range [1000, 300000], using default {} ms",
                    timeout, defaultTimeoutMs);
            return defaultTimeoutMs;
        }
        return timeout;
    }

    public SseEmitter proxy(String upstreamPath, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        AtomicReference<HttpURLConnection> connRef = new AtomicReference<>();

        Runnable cleanup = () -> {
            HttpURLConnection c = connRef.getAndSet(null);
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ignored) {
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        sseExecutor.submit(() -> {
            try {
                HttpURLConnection conn = openSseConnection(upstreamPath);
                connRef.set(conn);

                int status = conn.getResponseCode();
                if (status != 200) {
                    emitter.completeWithError(
                            new OpencodeApiException(status, upstreamPath,
                                    "SSE connection failed with status " + status));
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    streamEvents(reader, emitter, upstreamPath);
                }
                emitter.complete();
            } catch (Exception e) {
                if (connRef.get() != null) {
                    log.warn("SSE proxy error for {}: {}", upstreamPath, e.getMessage());
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                cleanup.run();
            }
        });

        return emitter;
    }

    private void streamEvents(BufferedReader reader, SseEmitter emitter,
                              String upstreamPath) throws Exception {
        StringBuilder dataBuilder = new StringBuilder();
        String eventName = null;
        String eventId = null;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (!dataBuilder.isEmpty()) {
                    SseEmitter.SseEventBuilder builder = SseEmitter.event()
                            .data(dataBuilder.toString());
                    if (eventName != null) builder.name(eventName);
                    if (eventId != null) builder.id(eventId);
                    emitter.send(builder);
                    dataBuilder.setLength(0);
                    eventName = null;
                    eventId = null;
                }
            } else if (line.startsWith("data:")) {
                if (!dataBuilder.isEmpty()) dataBuilder.append("\n");
                dataBuilder.append(line.substring(5).stripLeading());
            } else if (line.startsWith("event:")) {
                eventName = line.substring(6).stripLeading();
            } else if (line.startsWith("id:")) {
                eventId = line.substring(3).stripLeading();
            }
        }

        if (!dataBuilder.isEmpty()) {
            emitter.send(SseEmitter.event().data(dataBuilder.toString()));
        }
    }

    private HttpURLConnection openSseConnection(String path) throws Exception {
        URI uri = URI.create(config.getBaseUrl() + path);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Accept", "text/event-stream");
        if (config.hasAuth()) {
            String credentials = Base64.getEncoder().encodeToString(
                    (config.getUsername() + ":" + config.getPassword())
                            .getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + credentials);
        }
        conn.setConnectTimeout((int) Math.min(config.getTimeoutMs(), 5_000L));
        conn.setReadTimeout(0);
        return conn;
    }
}
