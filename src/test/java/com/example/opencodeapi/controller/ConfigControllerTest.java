package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.ConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfigService configService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void get_returnsOk() throws Exception {
        when(configService.get()).thenReturn(objectMapper.readTree("{\"theme\":\"dark\"}"));

        mockMvc.perform(get("/api/v1/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("dark"));
    }

    @Test
    void patch_returnsOk() throws Exception {
        when(configService.patch(any())).thenReturn(objectMapper.readTree("{\"theme\":\"light\"}"));

        mockMvc.perform(patch("/api/v1/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"light\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("light"));
    }

    @Test
    void getProviders_returnsOk() throws Exception {
        when(configService.getProviders()).thenReturn(objectMapper.readTree("[{\"id\":\"openai\"}]"));

        mockMvc.perform(get("/api/v1/config/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("openai"));
    }
}
