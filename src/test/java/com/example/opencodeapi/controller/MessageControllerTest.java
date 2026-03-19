package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(messageService.list("s1", null)).thenReturn(objectMapper.readTree("[{\"id\":\"m1\"}]"));

        mockMvc.perform(get("/api/v1/session/s1/message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("m1"));
    }

    @Test
    void list_withLimit_returnsOk() throws Exception {
        when(messageService.list("s1", 10)).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/session/s1/message").param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void send_returnsOk() throws Exception {
        when(messageService.send(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"m2\"}"));

        mockMvc.perform(post("/api/v1/session/s1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("m2"));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(messageService.get("s1", "m1")).thenReturn(objectMapper.readTree("{\"id\":\"m1\"}"));

        mockMvc.perform(get("/api/v1/session/s1/message/m1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("m1"));
    }

    @Test
    void promptAsync_returns204() throws Exception {
        doNothing().when(messageService).promptAsync(eq("s1"), any());

        mockMvc.perform(post("/api/v1/session/s1/prompt_async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void executeCommand_returnsOk() throws Exception {
        when(messageService.executeCommand(eq("s1"), any()))
                .thenReturn(objectMapper.readTree("{\"result\":\"ok\"}"));

        mockMvc.perform(post("/api/v1/session/s1/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void executeShell_returnsOk() throws Exception {
        when(messageService.executeShell(eq("s1"), any()))
                .thenReturn(objectMapper.readTree("{\"output\":\"done\"}"));

        mockMvc.perform(post("/api/v1/session/s1/shell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"ls\"}"))
                .andExpect(status().isOk());
    }
}
