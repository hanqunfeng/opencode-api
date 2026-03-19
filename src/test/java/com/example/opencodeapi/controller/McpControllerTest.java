package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.McpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(McpController.class)
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private McpService mcpService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void getStatus_returnsOk() throws Exception {
        when(mcpService.getStatus()).thenReturn(objectMapper.readTree("{\"servers\":[]}"));

        mockMvc.perform(get("/api/v1/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.servers").isArray());
    }

    @Test
    void add_returnsOk() throws Exception {
        when(mcpService.add(anyString(), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test-mcp\",\"config\":{\"command\":\"npx\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
