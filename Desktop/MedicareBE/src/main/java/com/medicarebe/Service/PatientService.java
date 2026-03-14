package com.medicarebe.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private FirebaseService firebaseService;

    // ✅ update verified flag
    public void updateEmailVerified(String uid, boolean verified) throws Exception {
        firebaseService.updateNodeValue(
                "patients/" + uid + "/emailVerified",
                verified
        );
    }

    // ✅ NEW — update email in realtime DB
    public void updateEmail(String uid, String email) throws Exception {
        firebaseService.updateNodeValue(
                "patients/" + uid + "/email",
                email
        );
    }
}