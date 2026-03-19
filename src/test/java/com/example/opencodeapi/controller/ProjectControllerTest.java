package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(projectService.list()).thenReturn(objectMapper.readTree("[{\"name\":\"proj1\"}]"));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("proj1"));
    }

    @Test
    void getCurrent_returnsOk() throws Exception {
        when(projectService.getCurrent()).thenReturn(objectMapper.readTree("{\"name\":\"current\"}"));

        mockMvc.perform(get("/api/v1/project/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("current"));
    }

    @Test
    void list_whenApiException_returnsErrorStatus() throws Exception {
        when(projectService.list()).thenThrow(new OpencodeApiException(500, "/project", "server error"));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void list_whenConnectionException_returns502() throws Exception {
        when(projectService.list()).thenThrow(
                new OpencodeConnectionException("/project", new RuntimeException("unreachable")));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
