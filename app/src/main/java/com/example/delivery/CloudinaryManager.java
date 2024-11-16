package com.example.delivery;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {

    private static Cloudinary cloudinary;

    public static void initCloudinary() {
        if (cloudinary == null) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dumglwhqw");
            config.put("api_key", "961571564217639");
            config.put("api_secret", "KHw3CNqszv1TL-Xt0T1ObKV8YHk");

            cloudinary = new Cloudinary(config);
        }
    }

    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            initCloudinary();
        }
        return cloudinary;
    }
}