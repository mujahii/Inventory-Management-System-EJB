/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.util;

/**
 * Singleton class to manage global configuration values for the Inventory Management System.
 */
public class ConfigManager {

    // The single shared instance
    private static ConfigManager instance;

    // Example configuration property
    private String appName = "Inventory Management System";

    // Private constructor prevents external instantiation
    private ConfigManager() {}

    /**
     * Returns the single ConfigManager instance, creating it if necessary.
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Gets the application name.
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets a new application name.
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

}