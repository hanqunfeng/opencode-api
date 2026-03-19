package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void find_returnsOk() throws Exception {
        when(fileService.find("*.java")).thenReturn(objectMapper.readTree("[\"App.java\"]"));

        mockMvc.perform(get("/api/v1/find").param("pattern", "*.java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void findFile_returnsOk() throws Exception {
        when(fileService.findFile(eq("test"), any(), any(), any(), any()))
                .thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/find/file").param("query", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void findSymbol_returnsOk() throws Exception {
        when(fileService.findSymbol("MyClass")).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/find/symbol").param("query", "MyClass"))
                .andExpect(status().isOk());
    }

    @Test
    void list_returnsOk() throws Exception {
        when(fileService.list("/src")).thenReturn(objectMapper.readTree("[\"file1.java\"]"));

        mockMvc.perform(get("/api/v1/file").param("path", "/src"))
                .andExpect(status().isOk());
    }

    @Test
    void getContent_returnsOk() throws Exception {
        when(fileService.getContent("/src/App.java"))
                .thenReturn(objectMapper.readTree("{\"content\":\"...\"}"));

        mockMvc.perform(get("/api/v1/file/content").param("path", "/src/App.java"))
                .andExpect(status().isOk());
    }

    @Test
    void getStatus_returnsOk() throws Exception {
        when(fileService.getStatus()).thenReturn(objectMapper.readTree("{\"tracked\":10}"));

        mockMvc.perform(get("/api/v1/file/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tracked").value(10));
    }
}
