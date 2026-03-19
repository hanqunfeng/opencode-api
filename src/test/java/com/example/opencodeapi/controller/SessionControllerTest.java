package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.SessionService;
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

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(sessionService.list()).thenReturn(objectMapper.readTree("[{\"id\":\"s1\"}]"));

        mockMvc.perform(get("/api/v1/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("s1"));
    }

    @Test
    void create_returnsOk() throws Exception {
        when(sessionService.create(any(), any())).thenReturn(objectMapper.readTree("{\"id\":\"new\"}"));

        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("new"));
    }

    @Test
    void getStatus_returnsOk() throws Exception {
        when(sessionService.getStatus()).thenReturn(objectMapper.readTree("{\"active\":true}"));

        mockMvc.perform(get("/api/v1/session/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(sessionService.get("s1")).thenReturn(objectMapper.readTree("{\"id\":\"s1\"}"));

        mockMvc.perform(get("/api/v1/session/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("s1"));
    }

    @Test
    void patchById_returnsOk() throws Exception {
        when(sessionService.patch(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"s1\"}"));

        mockMvc.perform(patch("/api/v1/session/s1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteById_returnsOk() throws Exception {
        when(sessionService.delete("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(delete("/api/v1/session/s1"))
                .andExpect(status().isOk());
    }

    @Test
    void getChildren_returnsOk() throws Exception {
        when(sessionService.getChildren("s1")).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/session/s1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTodo_returnsOk() throws Exception {
        when(sessionService.getTodo("s1")).thenReturn(objectMapper.readTree("{\"items\":[]}"));

        mockMvc.perform(get("/api/v1/session/s1/todo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void init_returnsOk() throws Exception {
        when(sessionService.init(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void fork_returnsOk() throws Exception {
        when(sessionService.fork(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"forked\"}"));

        mockMvc.perform(post("/api/v1/session/s1/fork")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageID\":\"m1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("forked"));
    }

    @Test
    void abort_returnsOk() throws Exception {
        when(sessionService.abort("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/abort"))
                .andExpect(status().isOk());
    }

    @Test
    void share_returnsOk() throws Exception {
        when(sessionService.share("s1")).thenReturn(objectMapper.readTree("{\"url\":\"http://...\"}"));

        mockMvc.perform(post("/api/v1/session/s1/share"))
                .andExpect(status().isOk());
    }

    @Test
    void unshare_returnsOk() throws Exception {
        when(sessionService.unshare("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(delete("/api/v1/session/s1/share"))
                .andExpect(status().isOk());
    }

    @Test
    void getDiff_withMessageId_returnsOk() throws Exception {
        when(sessionService.getDiff("s1", "m1")).thenReturn(objectMapper.readTree("{\"diff\":\"...\"}"));

        mockMvc.perform(get("/api/v1/session/s1/diff").param("messageID", "m1"))
                .andExpect(status().isOk());
    }

    @Test
    void summarize_returnsOk() throws Exception {
        when(sessionService.summarize(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"summary\":\"...\"}"));

        mockMvc.perform(post("/api/v1/session/s1/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"openai\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void revert_returnsOk() throws Exception {
        when(sessionService.revert(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/revert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageID\":\"m1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void unrevert_returnsOk() throws Exception {
        when(sessionService.unrevert("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/unrevert"))
                .andExpect(status().isOk());
    }

    @Test
    void respondToPermission_returnsOk() throws Exception {
        when(sessionService.respondToPermission(eq("s1"), eq("p1"), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/permissions/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"allow\":true}"))
                .andExpect(status().isOk());
    }
}
