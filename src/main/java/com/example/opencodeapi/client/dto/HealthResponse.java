package com.example.opencodeapi.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HealthResponse {
    private boolean healthy;
    private String version;
}
