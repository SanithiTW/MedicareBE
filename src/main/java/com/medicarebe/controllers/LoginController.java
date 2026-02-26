package com.medicarebe.controllers;

import com.medicarebe.Service.FirebaseService;
import com.medicarebe.Service.PatientService;
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

    @Autowired
    private PatientService patientService;

    // ================= LOGIN =================
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
                boolean mustChangePassword = false;

                // --- Admin ---
                if (firebaseService.getNodeValue("admin/" + uid + "/basic") != null) {
                    role = "Admin";
                }

                // --- Doctor ---
                else if (firebaseService.getNodeValue("doctors/" + uid) != null) {
                    role = "Doctor";

                    Object mustChangeObj =
                            firebaseService.getNodeValue("doctors/" + uid + "/mustChangePassword");

                    if (mustChangeObj instanceof Boolean) {
                        mustChangePassword = (Boolean) mustChangeObj;
                    } else if (mustChangeObj instanceof String) {
                        mustChangePassword = Boolean.parseBoolean((String) mustChangeObj);
                    }
                }

                // --- Pharmacy ---
                else if (firebaseService.getNodeValue("pharmacies/" + uid) != null) {
                    role = "Pharmacy";

                    Object statusObj =
                            firebaseService.getNodeValue("pharmacies/" + uid + "/status");

                    String status = "pending";

                    if (statusObj instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) statusObj;
                        Object valueObj = map.get("value");
                        if (valueObj != null) {
                            status = valueObj.toString().toLowerCase().trim();
                        }
                    } else if (statusObj instanceof Boolean) {
                        status = ((Boolean) statusObj) ? "accepted" : "pending";
                    } else if (statusObj instanceof String) {
                        status = ((String) statusObj).toLowerCase().trim();
                    }

                    if (!"accepted".equals(status)) {
                        return ResponseEntity
                                .status(403)
                                .body(Map.of("error",
                                        "Your pharmacy account is pending admin approval"));
                    }
                }

                // --- Patient ---
                else if (firebaseService.getNodeValue("patients/" + uid) != null) {
                    role = "Patient";
                }

                return ResponseEntity.ok(Map.of(
                        "uid", uid,
                        "role", role,
                        "mustChangePassword", mustChangePassword
                ));
            }

            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/doctor/password-updated")
    public ResponseEntity<?> passwordUpdated(@RequestBody Map<String, String> payload) {
        try {
            String uid = payload.get("uid");
            if (uid == null) return ResponseEntity.badRequest().body(Map.of("error", "UID required"));

            firebaseService.updateNodeValue("doctors/" + uid + "/mustChangePassword", false);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
