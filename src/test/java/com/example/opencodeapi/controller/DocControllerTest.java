package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.DocService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocController.class)
class DocControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocService docService;

    @Test
    void get_returnsHtml() throws Exception {
        when(docService.get()).thenReturn("<html><body>Hello</body></html>");

        mockMvc.perform(get("/api/v1/doc").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string("<html><body>Hello</body></html>"));
    }
}
