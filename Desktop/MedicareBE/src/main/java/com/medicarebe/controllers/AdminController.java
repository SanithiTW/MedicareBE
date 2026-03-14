package com.medicarebe.controllers;


import com.medicarebe.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendDoctorCredentials")
    public ResponseEntity<?> sendDoctorCredentials(
            @RequestBody Map<String, String> body) {

        try {
            String email = body.get("email");
            String password = body.get("password");
            String name = body.get("name");

            emailService.sendDoctorCredentials(email, name, password);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email sent"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/sendPharmacyCredentials")
    public ResponseEntity<?> sendPharmacyCredentials(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String password = payload.get("password");
            String name = payload.get("name");

            // Use the EmailService like you did for doctors
            emailService.sendPharmacyCredentials(email, name, password);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email sent"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}