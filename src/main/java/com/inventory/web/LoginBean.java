package com.inventory.web;

import com.inventory.ejb.UserServices;
import com.inventory.entity.User;
import com.inventory.entity.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("loginBean") // Explicitly naming it is good practice
@SessionScoped
public class LoginBean implements Serializable {

    private static final Logger logger = Logger.getLogger(LoginBean.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private UserServices userServices;

    // Login form fields
    private String username;
    private String password;

    // Current user session
    private User currentUser;
    private boolean loggedIn = false;

    @PostConstruct
    public void init() {
        logger.info("LoginBean initialized.");
        // If you need to check session on init, retrieve from session if exists
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                User storedUser = (User) session.getAttribute("currentUser");
                if (storedUser != null) {
                    this.currentUser = storedUser;
                    this.loggedIn = true;
                    logger.info("Existing user found in session: " + storedUser.getUsername());
                }
            }
        }
    }

    /**
     * Handles the user login process.
     * @return Navigation outcome based on login success and user role.
     */
    public String login() {
        try {
            logger.info("Login attempt for username: " + username);

            if (username == null || username.trim().isEmpty()) {
                addErrorMessage("Username is required.");
                return null;
            }

            if (password == null || password.trim().isEmpty()) {
                addErrorMessage("Password is required.");
                return null;
            }

            User user = userServices.authenticateUser(username.trim(), password);

            if (user != null) {
                this.currentUser = user;
                this.loggedIn = true;

                // Establish HTTP session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                session.setAttribute("currentUser", this.currentUser);
                session.setAttribute("loggedIn", true);

                logger.info("User logged in successfully: " + user.getUsername());
                addInfoMessage("Welcome back, " + user.getUsername() + "!");

                // Redirect based on user role
                if (user.getRole() == UserRole.ADMIN) { // Ensure this matches your UserRole enum exactly
                    return "/admin/dashboard.xhtml?faces-redirect=true";
                } else {
                    return "/user/dashboard.xhtml?faces-redirect=true";
                }
            } else {
                addErrorMessage("Invalid username or password. Please try again.");
                return null;
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Login error for username: " + username, e);
            addErrorMessage("Login failed due to an unexpected error. Please try again.");
            return null;
        }
    }

    /**
     * Handles the user logout process.
     * Invalidates the session and redirects to the login page.
     * @return Navigation outcome to the login page.
     */
    public String logout() {
        try {
            logger.info("User logging out: " + (currentUser != null ? currentUser.getUsername() : "unknown user"));

            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                session.invalidate(); // Invalidate the HTTP session
            }

            // Clear bean's state
            currentUser = null;
            loggedIn = false;
            clearLoginForm(); // Clear login fields

            addInfoMessage("You have been logged out successfully.");
            return "/login.xhtml?faces-redirect=true";

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Logout error", e);
            addErrorMessage("An error occurred during logout.");
            return "/login.xhtml?faces-redirect=true"; // Ensure redirect even on error
        }
    }

    /**
     * Clears the login form fields.
     */
    private void clearLoginForm() {
        username = null;
        password = null;
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

    // --- Security check methods ---
    public boolean isLoggedIn() {
        return loggedIn && currentUser != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == UserRole.ADMIN; // Ensure case matches enum
    }

    public boolean isUser() {
        return isLoggedIn() && currentUser.getRole() == UserRole.USER; // Ensure case matches enum
    }

    // --- Navigation methods (often used by filters or in JSF pages) ---
    public String redirectToLogin() {
        if (!isLoggedIn()) {
            return "/login.xhtml?faces-redirect=true";
        }
        return null; // Stay on the current page if already logged in
    }

    public String redirectToDashboard() {
        if (isLoggedIn()) {
            if (isAdmin()) {
                return "/admin/dashboard.xhtml?faces-redirect=true";
            } else {
                return "/user/dashboard.xhtml?faces-redirect=true";
            }
        }
        return "/login.xhtml?faces-redirect=true"; // Redirect to login if not logged in
    }

    // --- Getters and Setters for login form fields and current user ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole().toString() : "";
    }
    
    public int getCurrentYear() {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }
}