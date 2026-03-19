package com.example.opencodeapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class EventService {

    private final SseProxyHelper sseProxyHelper;

    public SseEmitter getEvent(Long timeout) {
        long resolvedTimeout = sseProxyHelper.resolveTimeout(timeout);
        return sseProxyHelper.proxy("/event", resolvedTimeout);
    }
}
