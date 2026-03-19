package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.DocApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocService {

    private final DocApiClient docApiClient;

    public String get() {
        return docApiClient.get();
    }
}
