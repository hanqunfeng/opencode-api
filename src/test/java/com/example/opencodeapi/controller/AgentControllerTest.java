package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.AgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(agentService.list()).thenReturn(objectMapper.readTree("[{\"id\":\"agent1\"}]"));

        mockMvc.perform(get("/api/v1/agent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void list_whenApiException_returnsErrorStatus() throws Exception {
        when(agentService.list()).thenThrow(new OpencodeApiException(404, "/api/v1/agent", "not found"));

        mockMvc.perform(get("/api/v1/agent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void list_whenConnectionException_returns502() throws Exception {
        when(agentService.list()).thenThrow(
                new OpencodeConnectionException("/api/v1/agent", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/agent"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
