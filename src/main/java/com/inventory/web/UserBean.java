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
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@SessionScoped
public class UserBean implements Serializable {
    
    private static final Logger logger = Logger.getLogger(UserBean.class.getName());
    private static final long serialVersionUID = 1L;
    
    @EJB
    private UserServices userServices;
    
    // User management fields
    private List<User> users;
    private User selectedUser;
    private User editUser;
    private boolean editMode = false;
    
    // Add user form fields
    private String newUsername;
    private String newEmail;
    private String newPassword;
    private UserRole newRole = UserRole.USER;
    
    // Edit user form fields
    private String editUsername;
    private String editEmail;
    private String editPassword;
    private UserRole editRole;
    
    // Search and filter
    private String searchKeyword;
    private UserRole filterRole;
    
    @PostConstruct
    public void init() {
        logger.info("UserBean initialized");
        loadAllUsers();
    }
    
    // Load all users
    public void loadAllUsers() {
        try {
            users = userServices.getAllUsers();
            logger.info("Loaded " + users.size() + " users");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading users", e);
            addErrorMessage("Failed to load users");
        }
    }

    // Add new user
    public void addUser() {
        try {
            logger.info("Adding new user: " + newUsername);
            
            if (!validateNewUserForm()) {
                return;
            }
            
            // Check if username already exists
            if (userServices.findUserByUsername(newUsername.trim()) != null) {
                addErrorMessage("Username already exists");
                return;
            }
            
            // Check if email already exists
            if (userServices.findUserByEmail(newEmail.trim()) != null) {
                addErrorMessage("Email already exists");
                return;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(newUsername.trim());
            newUser.setEmail(newEmail.trim());
            newUser.setPassword(newPassword); // Will be hashed in UserServices
            newUser.setRole(newRole);
            
            userServices.createUser(newUser);
            
            logger.info("User added successfully: " + newUser.getUsername());
            addInfoMessage("User added successfully!");
            
            // Clear form and reload users
            clearNewUserForm();
            loadAllUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding user", e);
            addErrorMessage("Failed to add user");
        }
    }
    
    // Edit user - prepare for editing
    public void editUser(User user) {
        try {
            logger.info("Preparing to edit user: " + user.getUsername());
            
            selectedUser = user;
            editUser = new User();
            editUser.setUserId(user.getUserId());
            editUser.setUsername(user.getUsername());
            editUser.setEmail(user.getEmail());
            editUser.setRole(user.getRole());
            editUser.setCreatedDate(user.getCreatedDate());
            editUser.setUpdatedDate(user.getUpdatedDate());
            
            // Set edit form fields
            editUsername = user.getUsername();
            editEmail = user.getEmail();
            editRole = user.getRole();
            editPassword = ""; // Don't show existing password
            
            editMode = true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error preparing user edit", e);
            addErrorMessage("Failed to prepare user for editing");
        }
    }
    
    // Update user
    public void updateUser() {
        try {
            logger.info("Updating user: " + editUsername);
            
            if (!validateEditUserForm()) {
                return;
            }
            
            // Check if username exists for other users
            User existingUser = userServices.findUserByUsername(editUsername.trim());
            if (existingUser != null && !existingUser.getUserId().equals(editUser.getUserId())) {
                addErrorMessage("Username already exists");
                return;
            }
            
            // Check if email exists for other users
            existingUser = userServices.findUserByEmail(editEmail.trim());
            if (existingUser != null && !existingUser.getUserId().equals(editUser.getUserId())) {
                addErrorMessage("Email already exists");
                return;
            }
            
            // Update user data
            editUser.setUsername(editUsername.trim());
            editUser.setEmail(editEmail.trim());
            editUser.setRole(editRole);
            
            // Update password only if provided
            if (editPassword != null && !editPassword.trim().isEmpty()) {
                editUser.setPassword(editPassword);
            }
            
            userServices.updateUser(editUser);
            
            logger.info("User updated successfully: " + editUser.getUsername());
            addInfoMessage("User updated successfully!");
            
            // Clear edit mode and reload users
            cancelEdit();
            loadAllUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating user", e);
            addErrorMessage("Failed to update user");
        }
    }
    
    // Delete user
    public void deleteUser(User user) {
        try {
            logger.info("Deleting user: " + user.getUsername());
            
            userServices.deleteUser(user.getUserId());
            
            logger.info("User deleted successfully: " + user.getUsername());
            addInfoMessage("User deleted successfully!");
            
            // Reload users
            loadAllUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting user", e);
            addErrorMessage("Failed to delete user");
        }
    }
    
    // Cancel edit
    public void cancelEdit() {
        editMode = false;
        selectedUser = null;
        editUser = null;
        clearEditUserForm();
    }
    
    // Search users
    public void searchUsers() {
        try {
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                loadAllUsers();
                return;
            }
            
            users = userServices.searchUsers(searchKeyword.trim());
            logger.info("Search returned " + users.size() + " users");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching users", e);
            addErrorMessage("Failed to search users");
        }
    }
    
    // Filter users by role
    public void filterUsers() {
        try {
            if (filterRole == null) {
                loadAllUsers();
                return;
            }
            
            users = userServices.getUsersByRole(filterRole);
            logger.info("Filter returned " + users.size() + " users with role " + filterRole);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering users", e);
            addErrorMessage("Failed to filter users");
        }
    }
    
    // Clear search and filter
    public void clearSearchAndFilter() {
        searchKeyword = null;
        filterRole = null;
        loadAllUsers();
    }
    
    // Validation methods
    private boolean validateNewUserForm() {
        boolean valid = true;
        
        if (newUsername == null || newUsername.trim().isEmpty()) {
            addErrorMessage("Username is required");
            valid = false;
        } else if (newUsername.trim().length() < 3) {
            addErrorMessage("Username must be at least 3 characters long");
            valid = false;
        }
        
        if (newEmail == null || newEmail.trim().isEmpty()) {
            addErrorMessage("Email is required");
            valid = false;
        } else if (!isValidEmail(newEmail.trim())) {
            addErrorMessage("Please enter a valid email address");
            valid = false;
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            addErrorMessage("Password is required");
            valid = false;
        } else if (newPassword.length() < 6) {
            addErrorMessage("Password must be at least 6 characters long");
            valid = false;
        }
        
        if (newRole == null) {
            addErrorMessage("Role is required");
            valid = false;
        }
        
        return valid;
    }
    
    private boolean validateEditUserForm() {
        boolean valid = true;
        
        if (editUsername == null || editUsername.trim().isEmpty()) {
            addErrorMessage("Username is required");
            valid = false;
        } else if (editUsername.trim().length() < 3) {
            addErrorMessage("Username must be at least 3 characters long");
            valid = false;
        }
        
        if (editEmail == null || editEmail.trim().isEmpty()) {
            addErrorMessage("Email is required");
            valid = false;
        } else if (!isValidEmail(editEmail.trim())) {
            addErrorMessage("Please enter a valid email address");
            valid = false;
        }
        
        // Password is optional for edit
        if (editPassword != null && !editPassword.isEmpty() && editPassword.length() < 6) {
            addErrorMessage("Password must be at least 6 characters long");
            valid = false;
        }
        
        if (editRole == null) {
            addErrorMessage("Role is required");
            valid = false;
        }
        
        return valid;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    // Utility methods
    private void clearNewUserForm() {
        newUsername = null;
        newEmail = null;
        newPassword = null;
        newRole = UserRole.USER;
    }
    
    private void clearEditUserForm() {
        editUsername = null;
        editEmail = null;
        editPassword = null;
        editRole = null;
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
    
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }
    
    // Statistics methods
    public long getTotalUsers() {
        return users != null ? users.size() : 0;
    }
    
    public long getAdminUsers() {
        return users != null ? users.stream().filter(u -> u.getRole() == UserRole.ADMIN).count() : 0;
    }
    
    public long getRegularUsers() {
        return users != null ? users.stream().filter(u -> u.getRole() == UserRole.USER).count() : 0;
    }
    
    // Statistics methods for admin overview
    public int getTotalUsersCount() {
        try {
            if (users == null) {
                loadAllUsers();
            }
            return users != null ? users.size() : 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total users count", e);
            return 0;
        }
    }
    
    public int getActiveUsers() {
        try {
            if (users == null) {
                loadAllUsers();
            }
            // For now, consider all users as active
            // In a real application, you might have a lastLogin field or isActive flag
            return users != null ? users.size() : 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting active users count", e);
            return 0;
        }
    }
    
    public String getSystemUptime() {
        try {
            // Simple uptime calculation (since JVM started)
            long uptimeMillis = System.currentTimeMillis() - 
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
            long days = uptimeMillis / (24 * 60 * 60 * 1000);
            long hours = (uptimeMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            
            if (days > 0) {
                return days + "d " + hours + "h";
            } else {
                return hours + "h";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating system uptime", e);
            return "N/A";
        }
    }
    
    // Method for viewing user details
    public void viewUser(User user) {
        this.selectedUser = user;
        logger.info("Viewing user: " + (user != null ? user.getUsername() : "null"));
    }
    
    // Getters and Setters
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public User getSelectedUser() {
        return selectedUser;
    }
    
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }
    
    public User getEditUser() {
        return editUser;
    }
    
    public void setEditUser(User editUser) {
        this.editUser = editUser;
    }
    
    public boolean isEditMode() {
        return editMode;
    }
    
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
    
    public String getNewUsername() {
        return newUsername;
    }
    
    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
    
    public String getNewEmail() {
        return newEmail;
    }
    
    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public UserRole getNewRole() {
        return newRole;
    }
    
    public void setNewRole(UserRole newRole) {
        this.newRole = newRole;
    }
    
    public String getEditUsername() {
        return editUsername;
    }
    
    public void setEditUsername(String editUsername) {
        this.editUsername = editUsername;
    }
    
    public String getEditEmail() {
        return editEmail;
    }
    
    public void setEditEmail(String editEmail) {
        this.editEmail = editEmail;
    }
    
    public String getEditPassword() {
        return editPassword;
    }
    
    public void setEditPassword(String editPassword) {
        this.editPassword = editPassword;
    }
    
    public UserRole getEditRole() {
        return editRole;
    }
    
    public void setEditRole(UserRole editRole) {
        this.editRole = editRole;
    }
    
    public String getSearchKeyword() {
        return searchKeyword;
    }
    
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
    
    public UserRole getFilterRole() {
        return filterRole;
    }
    
    public void setFilterRole(UserRole filterRole) {
        this.filterRole = filterRole;
    }
    
    public UserRole[] getUserRoles() {
        return UserRole.values();
    }
}