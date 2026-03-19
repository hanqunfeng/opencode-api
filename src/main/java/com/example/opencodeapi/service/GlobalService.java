package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.GlobalApiClient;
import com.example.opencodeapi.client.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class GlobalService {

    private final GlobalApiClient globalApiClient;
    private final SseProxyHelper sseProxyHelper;

    public HealthResponse getHealth() {
        return globalApiClient.getHealth();
    }

    public SseEmitter getGlobalEvent(Long timeout) {
        long resolvedTimeout = sseProxyHelper.resolveTimeout(timeout);
        return sseProxyHelper.proxy("/global/event", resolvedTimeout);
    }
}
