package com.medicarebe.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
public class SupabaseStorageService {

    private final String SUPABASE_URL = "https://zbsflpvwhxdrsdsssslo.supabase.co";
    private final String SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpic2ZscHZ3aHhkcnNkc3Nzc2xvIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NDU2MzQ3NywiZXhwIjoyMDgwMTM5NDc3fQ.pRzpDVAMsPtAPItmfDVqg5BJz8ahAdyrxBP8dpijglE";
    private final String BUCKET = "pharmacy-documents";

    public String uploadFile(String path, MultipartFile file) throws Exception {

        String safePath = path
                .replace("\\", "/")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._/-]", "");

        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + safePath;

        HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);

        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_SERVICE_KEY);

        // Use actual file MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/pdf"; // default MIME type
        }
        connection.setRequestProperty("Content-Type", mimeType);

        connection.setRequestProperty("x-upsert", "true");
        connection.setFixedLengthStreamingMode(file.getSize());

        try (OutputStream os = connection.getOutputStream()) {
            os.write(file.getBytes());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            try (var es = connection.getErrorStream()) {
                if (es != null) {
                    System.err.println(new String(es.readAllBytes()));
                }
            }
            throw new RuntimeException("Supabase upload failed, code: " + responseCode);
        }

        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + safePath;
    }

}
