package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.LspService;
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

@WebMvcTest(LspController.class)
class LspControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LspService lspService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void getStatus_returnsOk() throws Exception {
        when(lspService.getStatus()).thenReturn(objectMapper.readTree("{\"running\":true}"));

        mockMvc.perform(get("/api/v1/lsp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getStatus_whenApiException_returnsErrorStatus() throws Exception {
        when(lspService.getStatus()).thenThrow(new OpencodeApiException(404, "/api/v1/lsp", "not found"));

        mockMvc.perform(get("/api/v1/lsp"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getStatus_whenConnectionException_returns502() throws Exception {
        when(lspService.getStatus()).thenThrow(
                new OpencodeConnectionException("/api/v1/lsp", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/lsp"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
