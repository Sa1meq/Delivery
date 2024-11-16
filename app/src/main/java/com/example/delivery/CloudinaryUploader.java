package com.example.delivery;

import android.content.Context;
import android.net.Uri;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudinaryUploader {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Context context;

    public CloudinaryUploader(Context context) {
        this.context = context;
    }

    public void uploadImage(Uri imageUri, String userId, UploadCallback callback) {
        executorService.submit(() -> {
            try {

                Map<String, Object> uploadParams = new HashMap<>();
                uploadParams.put("public_id", userId);

                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

                if (inputStream != null) {

                    Map<String, Object> uploadResult = CloudinaryManager.getCloudinary()
                            .uploader()
                            .upload(inputStream, uploadParams);

                    inputStream.close();

                    String imageUrl = (String) uploadResult.get("secure_url");
                    callback.onUploadComplete(imageUrl);
                } else {
                    callback.onUploadComplete(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onUploadComplete(null);
            }
        });
    }

    public interface UploadCallback {
        void onUploadComplete(String imageUrl);
    }
}