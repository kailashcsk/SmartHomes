package com.smarthomes.listeners;

import com.smarthomes.util.ConfigUtil;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.nio.file.Files;
import java.util.logging.Logger;

@WebListener
public class ApplicationListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ApplicationListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Files.createDirectories(ConfigUtil.getUploadDirectory());
            LOGGER.info("Upload directory initialized: " + ConfigUtil.getUploadDirectory());
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize upload directory: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup code if needed
    }
}