package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.InstanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstanceController.class)
class InstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstanceService instanceService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void dispose_returnsOk() throws Exception {
        when(instanceService.dispose()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/instance/dispose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
