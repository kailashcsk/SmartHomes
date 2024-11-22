package com.smarthomes.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSetup {
    private static final Logger LOGGER = Logger.getLogger(DataSetup.class.getName());

    public static void main(String[] args) {
        try {
            // Verify Elasticsearch connection
            if (!ElasticsearchUtil.isRunning()) {
                LOGGER.severe("Elasticsearch is not running. Please start Elasticsearch first.");
                System.exit(1);
            }

            // Run data generation
            LOGGER.info("Starting data generation...");
            SmartHomeDataGenerator generator = new SmartHomeDataGenerator();
            generator.generateData();

            // Run data migration
            LOGGER.info("Starting data migration...");
            DataMigration migration = new DataMigration();
            migration.migrateExistingData();
            migration.verifyMigration();

            LOGGER.info("Setup completed successfully!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Setup failed: " + e.getMessage(), e);
            System.exit(1);
        }
    }
}