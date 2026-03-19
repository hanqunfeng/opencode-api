package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.TuiService;
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

@WebMvcTest(TuiController.class)
class TuiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TuiService tuiService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void appendPrompt_returnsOk() throws Exception {
        when(tuiService.appendPrompt("hello")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/append-prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void openHelp_returnsOk() throws Exception {
        when(tuiService.openHelp()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-help"))
                .andExpect(status().isOk());
    }

    @Test
    void openSessions_returnsOk() throws Exception {
        when(tuiService.openSessions()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-sessions"))
                .andExpect(status().isOk());
    }

    @Test
    void openThemes_returnsOk() throws Exception {
        when(tuiService.openThemes()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-themes"))
                .andExpect(status().isOk());
    }

    @Test
    void openModels_returnsOk() throws Exception {
        when(tuiService.openModels()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-models"))
                .andExpect(status().isOk());
    }

    @Test
    void submitPrompt_returnsOk() throws Exception {
        when(tuiService.submitPrompt()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/submit-prompt"))
                .andExpect(status().isOk());
    }

    @Test
    void clearPrompt_returnsOk() throws Exception {
        when(tuiService.clearPrompt()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/clear-prompt"))
                .andExpect(status().isOk());
    }

    @Test
    void executeCommand_returnsOk() throws Exception {
        when(tuiService.executeCommand("test")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/execute-command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void showToast_returnsOk() throws Exception {
        when(tuiService.showToast(any(), anyString(), anyString()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/show-toast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Info\",\"message\":\"Hello\",\"variant\":\"info\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void controlNext_returnsOk() throws Exception {
        when(tuiService.controlNext()).thenReturn(objectMapper.readTree("{\"type\":\"input\"}"));

        mockMvc.perform(get("/api/v1/tui/control/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("input"));
    }

    @Test
    void controlNext_whenTimeout_returnsEmptyOk() throws Exception {
        when(tuiService.controlNext()).thenThrow(
                new OpencodeConnectionException("/tui/control/next", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/tui/control/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void controlResponse_returnsOk() throws Exception {
        when(tuiService.controlResponse(any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/control/response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"yes\"}"))
                .andExpect(status().isOk());
    }
}
