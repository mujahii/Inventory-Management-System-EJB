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

@Named("adminLoginBean")
@SessionScoped
public class AdminLoginBean implements Serializable {

    private static final Logger logger = Logger.getLogger(AdminLoginBean.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private UserServices userServices;

    // Login form fields
    private String username;
    private String password;

    // Current admin user session
    private User currentAdmin;
    private boolean adminLoggedIn = false;

    @PostConstruct
    public void init() {
        logger.info("AdminLoginBean initialized.");
        // Check if admin is already logged in
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                User storedAdmin = (User) session.getAttribute("currentAdmin");
                if (storedAdmin != null && storedAdmin.getRole() == UserRole.ADMIN) {
                    this.currentAdmin = storedAdmin;
                    this.adminLoggedIn = true;
                    logger.info("Existing admin found in session: " + storedAdmin.getUsername());
                }
            }
        }
    }

    public String adminLogin() {
        logger.info("Admin login attempt for username: " + username);

        try {
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Username and password are required."));
                return null;
            }

            User user = userServices.authenticateUser(username.trim(), password.trim());
            
            if (user != null) {
                // Check if user has admin role
                if (user.getRole() == UserRole.ADMIN) {
                    this.currentAdmin = user;
                    this.adminLoggedIn = true;

                    // Store admin in session
                    FacesContext context = FacesContext.getCurrentInstance();
                    HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
                    session.setAttribute("currentAdmin", user);
                    session.setAttribute("adminLoggedIn", true);

                    logger.info("Admin login successful for: " + user.getUsername());
                    
                    // Clear form fields
                    this.username = "";
                    this.password = "";

                    // Redirect to admin dashboard
                    return "admin?faces-redirect=true";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Access Denied", "You do not have administrator privileges."));
                    logger.warning("Non-admin user attempted admin login: " + username);
                    return null;
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Login Failed", "Invalid username or password."));
                logger.warning("Admin login failed for username: " + username);
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during admin login", e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "An error occurred during login. Please try again."));
            return null;
        }
    }

    public String adminLogout() {
        logger.info("Admin logout for: " + (currentAdmin != null ? currentAdmin.getUsername() : "unknown"));

        // Clear session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            session.removeAttribute("currentAdmin");
            session.removeAttribute("adminLoggedIn");
            session.invalidate();
        }

        // Clear bean state
        this.currentAdmin = null;
        this.adminLoggedIn = false;
        this.username = "";
        this.password = "";

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success", "You have been logged out successfully."));

        return "admin-login?faces-redirect=true";
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn && currentAdmin != null && currentAdmin.getRole() == UserRole.ADMIN;
    }

    // Getters and Setters
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

    public User getCurrentAdmin() {
        return currentAdmin;
    }

    public String getCurrentAdminUsername() {
        return currentAdmin != null ? currentAdmin.getUsername() : "";
    }

    public int getCurrentYear() {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }
}
