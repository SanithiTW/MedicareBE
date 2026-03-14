package com.medicarebe.controllers;

import com.medicarebe.Service.FirebaseAdminService;
import com.medicarebe.Service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")


public class PharmacyLoginController {

    @Autowired
    private FirebaseAdminService firebaseAdminService;

    @PostMapping("/createPharmacy")
    public ResponseEntity<?> createPharmacyUser(@RequestBody Map<String, String> payload) {
        try {
            String uid = payload.get("uid");           // UID from Realtime DB
            String email = payload.get("email");
            String password = payload.get("password");

            if (uid == null || email == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "uid, email, password required"));
            }

            firebaseAdminService.createUserWithUid(uid, email, password);

            return ResponseEntity.ok(Map.of("success", true, "uid", uid));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
