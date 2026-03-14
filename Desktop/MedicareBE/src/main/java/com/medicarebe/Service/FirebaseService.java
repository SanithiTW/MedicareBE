package com.medicarebe.Service;

import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;



@Service
public class FirebaseService {

    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().getUserByEmail(email);
    }

    private final DatabaseReference dbRef;

    public FirebaseService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    // Create Firebase Authentication user
    public UserRecord createAuthUser(String email, String password, String displayName)
            throws FirebaseAuthException {

        UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(displayName)
                .setEmailVerified(false);

        return FirebaseAuth.getInstance().createUser(req);
    }
    public UserRecord createGoogleUser(String email, String name) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setDisplayName(name)
                .setEmailVerified(true);

        return FirebaseAuth.getInstance().createUser(request);
    }


    // Save data to Realtime DB
    public void saveToRealtimeDb(String path, Map<String, Object> data) throws Exception {
        DatabaseReference ref = dbRef.child(path);
        ApiFuture<Void> future = ref.setValueAsync(data);
        future.get();
    }

    // Push new child
    public String pushToRealtimeDb(String path, Map<String, Object> data) throws Exception {
        DatabaseReference ref = dbRef.child(path).push();
        ApiFuture<Void> future = ref.setValueAsync(data);
        future.get();
        return ref.getKey();
    }

    // Upload file to Storage
    public String uploadFile(String pathInBucket, MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        String contentType = file.getContentType();

        com.google.cloud.storage.Blob blob = StorageClient.getInstance().bucket()
                .create(pathInBucket, is, contentType);

        blob.createAcl(
                com.google.cloud.storage.Acl.of(
                        com.google.cloud.storage.Acl.User.ofAllUsers(),
                        com.google.cloud.storage.Acl.Role.READER
                )
        );

        return blob.getMediaLink();
    }

    // Move node (approve pharmacy)
    public void moveNode(String fromPath, String toPath) throws Exception {
        DatabaseReference fromRef = dbRef.child(fromPath);
        DatabaseReference toRef = dbRef.child(toPath);

        // Read value manually (this works in all Firebase Admin SDK versions)
        final Object[] dataHolder = new Object[1];

        fromRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }
                dataHolder[0] = snapshot.getValue();

                // Write the data to new path
                toRef.setValueAsync(dataHolder[0]);

                // Remove old node
                fromRef.removeValueAsync();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error reading Firebase: " + error.getMessage());
            }

            public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
                return FirebaseAuth.getInstance().getUserByEmail(email);
            }

        });
    }

    public Map<String, Object> getNodeValue(String path) throws Exception {
        DatabaseReference ref = dbRef.child(path);
        final Object[] dataHolder = new Object[1];

        final Exception[] exHolder = new Exception[1];
        final Object lock = new Object();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dataHolder[0] = snapshot.exists() ? snapshot.getValue() : null;
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                exHolder[0] = new Exception(error.getMessage());
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            lock.wait(5000); // wait for 5 sec max
        }

        if (exHolder[0] != null) throw exHolder[0];

        if (dataHolder[0] == null) return null;

        return (Map<String, Object>) dataHolder[0];
    }


    public void updateUserEmail(String uid, String newEmail) throws Exception {
        UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid)
                .setEmail(newEmail);
        FirebaseAuth.getInstance().updateUser(request);
    }

    public void sendEmailVerification(String uid) throws FirebaseAuthException {
        UserRecord user = FirebaseAuth.getInstance().getUser(uid);

        // Generate email verification link
        String link = FirebaseAuth.getInstance().generateEmailVerificationLink(user.getEmail());

        // TODO: Send the link by Gmail SMTP or any email API
        // For now, return link to frontend OR send by your email service
        System.out.println("EMAIL VERIFICATION LINK: " + link);
    }

    public UserRecord createAuthUserWithUid(UserRecord.CreateRequest request) throws Exception {
        return com.google.firebase.auth.FirebaseAuth.getInstance().createUser(request);
    }

    public void updateNodeValue(String path, Object value) throws Exception {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.setValueAsync(value).get(); // wait for completion
    }



}
