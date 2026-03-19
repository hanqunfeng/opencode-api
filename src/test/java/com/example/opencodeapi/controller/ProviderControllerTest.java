package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.ProviderService;
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

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(providerService.list()).thenReturn(objectMapper.readTree("[{\"id\":\"openai\"}]"));

        mockMvc.perform(get("/api/v1/provider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("openai"));
    }

    @Test
    void getAuth_returnsOk() throws Exception {
        when(providerService.getAuth()).thenReturn(objectMapper.readTree("{\"authenticated\":true}"));

        mockMvc.perform(get("/api/v1/provider/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true));
    }

    @Test
    void oauthAuthorize_returnsOk() throws Exception {
        when(providerService.oauthAuthorize("openai"))
                .thenReturn(objectMapper.readTree("{\"url\":\"https://auth.example.com\"}"));

        mockMvc.perform(post("/api/v1/provider/openai/oauth/authorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://auth.example.com"));
    }

    @Test
    void oauthCallback_returnsOk() throws Exception {
        when(providerService.oauthCallback(eq("openai"), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/provider/openai/oauth/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"abc123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
