package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.DocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String get() {
        return docService.get();
    }
}
