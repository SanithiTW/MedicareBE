package com.medicarebe.controllers;

import com.medicarebe.Service.FirebaseService;
import com.google.firebase.auth.UserRecord;
import com.medicarebe.Service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "*")
public class RegistrationController {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;


    @GetMapping
    public String test() {
        return "Registration API is working!";
    }


    // -------------------------------------------------------------
    // 1) GOOGLE SIGN-IN (unchanged)
    // -------------------------------------------------------------
    @PostMapping("/patient/google")
    public ResponseEntity<?> patientGoogleSignIn(@RequestBody Map<String, String> payload) {
        try {
            String idTokenString = payload.get("idToken");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new JacksonFactory()
            )
                    .setAudience(List.of("1080448394290-dk4th5n17iljbv4fco2v9kaf2j8t73as.apps.googleusercontent.com"))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google ID token"));
            }

            Payload tokenPayload = idToken.getPayload();
            String email = tokenPayload.getEmail();
            String name = (String) tokenPayload.get("name");

            UserRecord userRecord;
            try {
                userRecord = firebaseService.getUserByEmail(email);
            } catch (Exception ex) {
                userRecord = null;
            }

            if (userRecord == null) {
                userRecord = firebaseService.createGoogleUser(email, name);
            }

            String uid = userRecord.getUid();

            // Save basic info if missing
            Map<String, Object> basic = firebaseService.getNodeValue("patients/" + uid + "/basic");
            if (basic == null) {
                basic = new HashMap<>();
                basic.put("role", "Patient");
                basic.put("name", name);
                basic.put("email", email);
                basic.put("createdAt", System.currentTimeMillis());
                firebaseService.saveToRealtimeDb("patients/" + uid + "/basic", basic);
            }

            return ResponseEntity.ok(Map.of("uid", uid));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



    // -------------------------------------------------------------
// 2) PATIENT STEP 1 (FIXED: check if user exists before creating)
// -------------------------------------------------------------
    @PostMapping("/patient/step1")
    public ResponseEntity<?> patientStep1(@RequestBody Map<String, Object> payload) {
        try {
            String email = (String) payload.get("email");
            String password = (String) payload.get("password");
            String name = (String) payload.get("name");

            UserRecord userRecord;

            // ✔️ Check if user already exists
            try {
                userRecord = firebaseService.getUserByEmail(email);
            } catch (Exception ex) {
                userRecord = null;
            }

            // ✅ If user exists → return 400 error instead of continuing
            if (userRecord != null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Email already registered. Please log in."));
            }

            // ✔️ If NOT found → create new Firebase Auth user
            userRecord = firebaseService.createAuthUser(email, password, name);

            String uid = userRecord.getUid();

            // ✔️ Ensure basic node exists
            Map<String, Object> basic = firebaseService.getNodeValue("patients/" + uid + "/basic");
            if (basic == null) {
                basic = new HashMap<>();
                basic.put("role", "Patient");
                basic.put("name", name);
                basic.put("email", email);
                basic.put("createdAt", System.currentTimeMillis());
                firebaseService.saveToRealtimeDb("patients/" + uid + "/basic", basic);
            }

            return ResponseEntity.ok(Map.of("uid", uid));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    // -------------------------------------------------------------
    // 3) PATIENT COMPLETE (unchanged)
    // -------------------------------------------------------------
    @PostMapping("/patient/{uid}/complete")
    public ResponseEntity<?> patientComplete(@PathVariable String uid, @RequestBody Map<String, Object> payload) {
        try {
            System.out.println("Received patient profile data: " + payload);
            payload.put("completedAt", System.currentTimeMillis());
            firebaseService.saveToRealtimeDb("patients/" + uid + "/profile", payload);
            firebaseService.saveToRealtimeDb("patients/" + uid + "/status", Map.of("completed", true));



            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

    }

    // -------------------------------------------------------------
    // PHARMACY ENDPOINTS (unchanged)
    // -------------------------------------------------------------
    @PostMapping("/pharmacy/step1")
    public ResponseEntity<?> pharmacyStep1(@RequestBody Map<String, Object> payload) {
        try {
            payload.put("role", "Pharmacy");
            payload.put("status", Map.of(
                    "value", "pending",
                    "updatedAt", System.currentTimeMillis()
            ));
            payload.put("createdAt", System.currentTimeMillis());
            String key = firebaseService.pushToRealtimeDb("pharmacies", payload);

            return ResponseEntity.ok(Map.of("pendingId", key));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(
            value = "/pharmacy/{pendingId}/complete",
            consumes = "multipart/form-data"
    )

    public ResponseEntity<?> pharmacyComplete(
            @PathVariable String pendingId,

            @RequestParam String pharmacyname,
            @RequestParam String officialEmail,
            @RequestParam String phone,
            @RequestParam String name,
            @RequestParam String businessRegNo,
            @RequestParam String licenseNo,
            @RequestParam String licenseExpiryDate,
            @RequestParam(required = false) String taxId,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String province,
            @RequestParam String postalCode,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam String deliverySupport,
            @RequestParam(required = false, defaultValue = "") String deliveryRange,
            @RequestParam String openingTime,
            @RequestParam String closingTime,
            @RequestParam String operatingDays,
            @RequestParam String services,

            @RequestParam(value = "regCertificate", required = false) MultipartFile regCertificate,
            @RequestParam(value = "pharmacistLicenseCopy", required = false) MultipartFile pharmacistLicenseCopy,
            @RequestParam(value = "frontPhoto", required = false) MultipartFile frontPhoto,
            @RequestParam(value = "ownerID", required = false) MultipartFile ownerID
    ) {
        try {
            Map<String, Object> updates = new HashMap<>();

            updates.put("pharmacyname", pharmacyname);
            updates.put("officialEmail", officialEmail);
            updates.put("phone", phone);
            updates.put("name", name);
            updates.put("businessRegNo", businessRegNo);
            updates.put("licenseNo", licenseNo);
            updates.put("licenseExpiryDate", licenseExpiryDate);
            updates.put("taxId", taxId);
            updates.put("address", address);
            updates.put("city", city);
            updates.put("province", province);
            updates.put("postalCode", postalCode);
            updates.put("latitude", latitude);
            updates.put("longitude", longitude);
            updates.put("deliverySupport", deliverySupport);
            updates.put("deliveryRange", deliveryRange);
            updates.put("openingTime", openingTime);
            updates.put("closingTime", closingTime);
            updates.put("operatingDays", operatingDays);
            updates.put("services", services);

            // 1️⃣ Set status
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("value", "pending");
            statusMap.put("updatedAt", System.currentTimeMillis());
            updates.put("status", statusMap);

// 2️⃣ Add completedAt
            updates.put("completedAt", System.currentTimeMillis());




            // Supabase uploads (UNCHANGED)
            if (regCertificate != null && !regCertificate.isEmpty()) {
                updates.put("regCertificateUrl",
                        supabaseStorageService.uploadFile(
                                "pharmacies/" + pendingId + "/regCertificate.pdf",
                                regCertificate));
            }

            if (pharmacistLicenseCopy != null && !pharmacistLicenseCopy.isEmpty()) {
                updates.put("pharmacistLicenseUrl",
                        supabaseStorageService.uploadFile(
                                "pharmacies/" + pendingId + "/pharmacistLicense.pdf",
                                pharmacistLicenseCopy));
            }

            if (frontPhoto != null && !frontPhoto.isEmpty()) {
                updates.put("frontPhotoUrl",
                        supabaseStorageService.uploadFile(
                                "pharmacies/" + pendingId + "/frontPhoto.jpg",
                                frontPhoto));
            }

            if (ownerID != null && !ownerID.isEmpty()) {
                updates.put("ownerIdUrl",
                        supabaseStorageService.uploadFile(
                                "pharmacies/" + pendingId + "/ownerID.pdf",
                                ownerID));
            }

            firebaseService.saveToRealtimeDb("pharmacies/" + pendingId, updates);


            Map<String, Object> notifStatus = new HashMap<>();
            notifStatus.put("value", "pending");
            notifStatus.put("createdAt", System.currentTimeMillis());

            firebaseService.saveToRealtimeDb(
                    "admin/notifications/pharmacyRequests/" + pendingId,
                    Map.of(
                            "pendingId", pendingId,
                            "status", notifStatus
                    )
            );


            return ResponseEntity.ok(Map.of("status", "pending approval"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/createPharmacy")
    public ResponseEntity<?> createPharmacy(@RequestBody Map<String, String> payload) {
        try {
            String uid = payload.get("uid");        // Use pendingId as UID
            String email = payload.get("email");
            String password = payload.get("password");

            // 1️⃣ Create Auth user with specific UID
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setUid(uid)
                    .setEmail(email)
                    .setPassword(password);

            UserRecord user = firebaseService.createAuthUserWithUid(request);

            return ResponseEntity.ok(Map.of("success", true, "uid", user.getUid()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }




}
