package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.LogService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void write_returnsOk() throws Exception {
        when(logService.write(anyString(), anyString(), anyString(), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"service\":\"test\",\"level\":\"info\",\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
