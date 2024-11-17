package com.smarthomes.util;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.http.Part;

public class FileUploadUtil {
    private static final Logger LOGGER = Logger.getLogger(FileUploadUtil.class.getName());
    private static final String UPLOAD_DIRECTORY = "customer_service_images";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = { ".jpg", ".jpeg", ".png" };

    public static FileInfo saveImage(Part filePart) throws IOException {
        validateFile(filePart);

        // Save in both Tomcat webapps (for HTTP access) and customer_service_images
        // (for OpenAI access)
        String catalina_home = System.getProperty("catalina.home");

        // Generate unique filename
        String fileName = UUID.randomUUID().toString() + getFileExtension(filePart.getSubmittedFileName());

        // Path for web access
        String webappPath = catalina_home + File.separator +
                "webapps" + File.separator +
                "smarthomes-backend" + File.separator +
                UPLOAD_DIRECTORY;

        // Path for OpenAI access
        String openaiPath = catalina_home + File.separator + UPLOAD_DIRECTORY;

        // Create directories if they don't exist
        new File(webappPath).mkdirs();
        new File(openaiPath).mkdirs();

        // Save file in webapp directory
        String webappFilePath = webappPath + File.separator + fileName;
        filePart.write(webappFilePath);

        // Save file in OpenAI directory
        String openaiFilePath = openaiPath + File.separator + fileName;
        filePart.write(openaiFilePath);

        LOGGER.info("File saved successfully at web path: " + webappFilePath);
        LOGGER.info("File saved successfully at OpenAI path: " + openaiFilePath);

        return new FileInfo(
                UPLOAD_DIRECTORY + "/" + fileName, // web path
                openaiFilePath // file system path
        );
    }

    // Add FileInfo inner class
    public static class FileInfo {
        private final String webPath;
        private final String filePath;

        public FileInfo(String webPath, String filePath) {
            this.webPath = webPath;
            this.filePath = filePath;
        }

        public String getWebPath() {
            return webPath;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private static void validateFile(Part filePart) throws IOException {
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        String fileName = filePart.getSubmittedFileName();
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileName.toLowerCase().endsWith(ext)) {
                validExtension = true;
                break;
            }
        }
        if (!validExtension) {
            throw new IOException("Invalid file type. Allowed types: JPG, JPEG, PNG");
        }

        String contentType = filePart.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new IOException("Invalid content type. Only images are allowed.");
        }
    }
}