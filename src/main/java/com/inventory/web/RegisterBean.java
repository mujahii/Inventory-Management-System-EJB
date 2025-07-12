/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.web;

import com.inventory.ejb.UserServices;
import com.inventory.entity.User;
import com.inventory.entity.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Pattern; // Import for email validation

@Named("registerBean")
@RequestScoped
public class RegisterBean implements Serializable {

    private static final Logger logger = Logger.getLogger(RegisterBean.class.getName());

    @EJB
    private UserServices userServices;

    private User user; // To hold the registration data
    private String confirmPassword; // To confirm password

    @PostConstruct
    public void init() {
        user = new User();
        logger.info("RegisterBean initialized.");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public int getCurrentYear() {
        return java.time.Year.now().getValue();
    }

    /**
     * Handles the user registration process.
     * Performs validation, checks for existing username/email,
     * creates the user, and redirects to the login page on success.
     * @return Navigation outcome.
     */
    public String register() {
        try {
            logger.info("Attempting to register user: " + user.getUsername());

            // Perform server-side validation
            if (!validateRegistrationForm()) {
                return null; // Stay on the same page
            }

            // Check if username already exists
            if (userServices.usernameExists(user.getUsername().trim())) {
                addErrorMessage("Username '" + user.getUsername().trim() + "' already exists. Please choose a different one.");
                return null;
            }

            // Check if email already exists
            if (userServices.emailExists(user.getEmail().trim())) {
                addErrorMessage("Email '" + user.getEmail().trim() + "' is already registered. Please use a different email.");
                return null;
            }

            // Set default role (using uppercase as per common convention, assuming UserRole enum will be updated)
            user.setRole(UserRole.USER); // Assuming UserRole.user is the correct enum constant for default users
            
            // Create the user via EJB service
            userServices.createUser(user);

            // Add a success message that persists across redirects
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            addInfoMessage("Registration successful! You can now log in with your new account.");

            logger.info("User registered successfully: " + user.getUsername());
            
            // Redirect to login page
            return "login?faces-redirect=true";

        } catch (Exception e) {
            logger.severe("Registration failed for user " + user.getUsername() + ": " + e.getMessage());
            addErrorMessage("Registration failed due to an unexpected error. Please try again.");
            return null; // Stay on the same page
        }
    }

    /**
     * Performs server-side validation for registration form fields.
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateRegistrationForm() {
        boolean valid = true;

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            addErrorMessage("Username is required.");
            valid = false;
        } else if (user.getUsername().trim().length() < 3) {
            addErrorMessage("Username must be at least 3 characters long.");
            valid = false;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            addErrorMessage("Email is required.");
            valid = false;
        } else if (!isValidEmail(user.getEmail().trim())) {
            addErrorMessage("Please enter a valid email address.");
            valid = false;
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            addErrorMessage("Password is required.");
            valid = false;
        } else if (user.getPassword().length() < 6) {
            addErrorMessage("Password must be at least 6 characters long.");
            valid = false;
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            addErrorMessage("Please confirm your password.");
            valid = false;
        } else if (!user.getPassword().equals(confirmPassword)) {
            addErrorMessage("Passwords do not match.");
            valid = false;
        }
        return valid;
    }

    /**
     * Basic email format validation.
     * @param email The email string to validate.
     * @return true if the email format is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        // A more robust regex could be used, but this matches your existing simple check
        return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    /**
     * Adds an error message to FacesContext.
     * @param message The error message to display.
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    /**
     * Adds an informational message to FacesContext.
     * @param message The info message to display.
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }
}