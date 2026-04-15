package com.berlin.aetherflow.controller;

import com.berlin.aetherflow.exception.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "UP");
        payload.put("application", applicationName);
        return Result.success(payload);
    }
}
