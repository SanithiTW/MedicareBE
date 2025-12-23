package com.medicarebe.controllers;

import com.medicarebe.Service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/email")
    public ResponseEntity<?> loginWithEmail(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and password required"));
            }

            String firebaseApiKey = "AIzaSyAX4EA-gXGDkwV8DxUxrIRdzU-YdX2cI6Q";
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;

            Map<String, Object> req = Map.of(
                    "email", email,
                    "password", password,
                    "returnSecureToken", true
            );

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, req, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map body = response.getBody();
                String uid = (String) body.get("localId");

                String role = "Patient";
                Map<String, Object> basic = firebaseService.getNodeValue("patients/" + uid + "/basic");

                if (basic != null && basic.containsKey("role")) role = (String) basic.get("role");
                else if (firebaseService.getNodeValue("admin/" + uid + "/basic") != null) role = "Admin";
                else if (firebaseService.getNodeValue("doctors/" + uid + "/basic") != null) role = "Doctor";
                else if (firebaseService.getNodeValue("pharmacies/" + uid + "/basic") != null) role = "Pharmacy";

                return ResponseEntity.ok(Map.of("uid", uid, "role", role));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
