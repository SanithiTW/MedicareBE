package com.medicarebe.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PredictController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_URL = "http://localhost:5000/predict";

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, Object> req) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

            ResponseEntity<Object> response =
                    restTemplate.exchange(PYTHON_URL, HttpMethod.POST, entity, Object.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Spring Boot could not reach Python service",
                    "details", e.getMessage()
            ));
        }
    }
}
