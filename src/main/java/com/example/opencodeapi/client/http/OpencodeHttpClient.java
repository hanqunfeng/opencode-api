package com.example.opencodeapi.client.http;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class OpencodeHttpClient {

    private final RestClient restClient;
    private final RestClient blockingRestClient;
    private final OpencodeClientConfig config;

    public OpencodeHttpClient(OpencodeClientConfig config) {
        this.config = config;

        RestClient.Builder baseBuilder = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (config.hasAuth()) {
            String credentials = Base64.getEncoder().encodeToString(
                    (config.getUsername() + ":" + config.getPassword())
                            .getBytes(StandardCharsets.UTF_8));
            baseBuilder.defaultHeader("Authorization", "Basic " + credentials);
        }

        SimpleClientHttpRequestFactory normalFactory = new SimpleClientHttpRequestFactory();
        normalFactory.setConnectTimeout(Duration.ofMillis(Math.min(config.getTimeoutMs(), 5_000L)));
        normalFactory.setReadTimeout(Duration.ofMillis(config.getTimeoutMs()));
        this.restClient = baseBuilder.clone().requestFactory(normalFactory).build();

        SimpleClientHttpRequestFactory blockingFactory = new SimpleClientHttpRequestFactory();
        blockingFactory.setConnectTimeout(Duration.ofMillis(Math.min(config.getBlockingTimeoutMs(), 5_000L)));
        blockingFactory.setReadTimeout(Duration.ofMillis(config.getBlockingTimeoutMs()));
        this.blockingRestClient = baseBuilder.clone().requestFactory(blockingFactory).build();
    }

    public <T> T get(String path, Class<T> type) {
        return get(path, null, type);
    }

    public <T> T get(String path, Map<String, String> queryParams, Class<T> type) {
        try {
            return restClient.get()
                    .uri(buildUri(path, queryParams))
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T post(String path, Object body, Class<T> type) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(path);
            if (body != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }
            return spec.retrieve().body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public void postNoContent(String path, Object body) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(path);
            if (body != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }
            spec.retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T patch(String path, Object body, Class<T> type) {
        try {
            return restClient.patch()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T put(String path, Object body, Class<T> type) {
        try {
            return restClient.put()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T delete(String path, Class<T> type) {
        try {
            return restClient.delete()
                    .uri(path)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public String getHtml(String path) {
        try {
            return restClient.get()
                    .uri(path)
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public String readFirstSseEvent(String path) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(() -> doReadFirstSseEvent(path));
            return future.get(config.getBlockingTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new OpencodeConnectionException(path,
                    new RuntimeException("Timeout waiting for first SSE event on " + path));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OpencodeApiException oae) throw oae;
            if (cause instanceof OpencodeConnectionException oce) throw oce;
            throw new OpencodeConnectionException(path, cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpencodeConnectionException(path, e);
        } finally {
            executor.shutdownNow();
        }
    }

    private String doReadFirstSseEvent(String path) throws Exception {
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
        conn.setReadTimeout((int) config.getBlockingTimeoutMs());

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new OpencodeApiException(status, path, "SSE connection failed with status " + status);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder event = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() && !event.isEmpty()) {
                    return event.toString().trim();
                }
                if (!line.isEmpty()) {
                    if (!event.isEmpty()) event.append("\n");
                    event.append(line);
                }
            }
            return event.isEmpty() ? "" : event.toString().trim();
        } finally {
            conn.disconnect();
        }
    }

    public <T> T getBlocking(String path, Class<T> type) {
        try {
            return blockingRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    private String buildUri(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return path;
        StringBuilder sb = new StringBuilder(path);
        sb.append("?");
        queryParams.forEach((k, v) -> {
            if (v != null) {
                sb.append(java.net.URLEncoder.encode(k, StandardCharsets.UTF_8))
                  .append("=")
                  .append(java.net.URLEncoder.encode(v, StandardCharsets.UTF_8))
                  .append("&");
            }
        });
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private OpencodeApiException toApiException(String path, RestClientResponseException e) {
        return new OpencodeApiException(e.getStatusCode().value(), path, e.getResponseBodyAsString());
    }
}
