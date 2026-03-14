package com.medicarebe.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Service
public class FirebaseAdminService {

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("firebase-service-account.json"); // your service account

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserRecord createUserWithUid(String uid, String email, String password) throws Exception {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setUid(uid)
                .setEmail(email)
                .setPassword(password);

        return FirebaseAuth.getInstance().createUser(request);
    }


}
