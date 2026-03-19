package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.service.GlobalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalController.class)
class GlobalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalService globalService;

    @Test
    void health_returnsOk() throws Exception {
        HealthResponse health = new HealthResponse();
        health.setHealthy(true);
        health.setVersion("1.0.0");
        when(globalService.getHealth()).thenReturn(health);

        mockMvc.perform(get("/api/v1/global/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.healthy").value(true))
                .andExpect(jsonPath("$.data.version").value("1.0.0"));
    }

    @Test
    void event_returnsSseEmitter() throws Exception {
        when(globalService.getGlobalEvent(any())).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/global/event"))
                .andExpect(request().asyncStarted());
    }
}
