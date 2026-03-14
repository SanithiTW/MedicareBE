package com.medicarebe.controllers;

import com.medicarebe.Service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {



    @Autowired
    private PatientService patientService;

    // ================= VERIFY EMAIL =================
    @PatchMapping("/{uid}/verify-email")
    public ResponseEntity<?> verifyEmail(
            @PathVariable String uid,
            @RequestBody Map<String, Object> body) throws Exception {

        boolean verified = Boolean.TRUE.equals(body.get("emailVerified"));

        patientService.updateEmailVerified(uid, verified);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verification updated"
        ));
    }

    // ================= UPDATE EMAIL =================
    @PatchMapping("/{uid}/update-email")
    public ResponseEntity<?> updateEmail(
            @PathVariable String uid,
            @RequestBody Map<String, Object> body) throws Exception {

        String email = (String) body.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
        }

        patientService.updateEmail(uid, email);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email updated in realtime DB"
        ));
    }
}