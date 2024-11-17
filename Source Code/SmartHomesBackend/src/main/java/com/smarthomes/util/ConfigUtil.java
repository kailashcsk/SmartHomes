package com.smarthomes.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigUtil {
    // Base directory for uploads (you can change this to your preferred location)
    private static final String BASE_UPLOAD_DIR = System.getProperty("user.home") + "/smarthomes/uploads";

    // Maximum allowed file size (5MB)
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Allowed file types
    public static final String[] ALLOWED_FILE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/gif"
    };

    public static Path getUploadDirectory() {
        return Paths.get(BASE_UPLOAD_DIR);
    }
}