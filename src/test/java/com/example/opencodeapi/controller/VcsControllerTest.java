package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.VcsService;
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

@WebMvcTest(VcsController.class)
class VcsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VcsService vcsService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void get_returnsOk() throws Exception {
        when(vcsService.get()).thenReturn(objectMapper.readTree("{\"branch\":\"main\"}"));

        mockMvc.perform(get("/api/v1/vcs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void get_whenApiException_returnsErrorStatus() throws Exception {
        when(vcsService.get()).thenThrow(new OpencodeApiException(404, "/api/v1/vcs", "not found"));

        mockMvc.perform(get("/api/v1/vcs"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void get_whenConnectionException_returns502() throws Exception {
        when(vcsService.get()).thenThrow(
                new OpencodeConnectionException("/api/v1/vcs", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/vcs"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
