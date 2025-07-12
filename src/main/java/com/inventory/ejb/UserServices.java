/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.inventory.ejb;

import com.inventory.entity.UserRole;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.inventory.dao.UserDAO;
import com.inventory.entity.User;
import jakarta.inject.Inject;
import java.util.List;

@Stateless

public class UserServices {

    @Inject
    private UserDAO userDAO;

    public boolean login(String email, String password) {
        User user = userDAO.findByEmail(email);
        return user != null && verifyPassword(password, user.getPassword());
    }

    public void register(User user) {
        createUser(user);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    private static final Logger logger = Logger.getLogger(UserServices.class.getName());
    
    @PersistenceContext(unitName = "InventoryPU")
    private EntityManager em;
    
    // Create new user
    public void createUser(User user) {
        try {
            logger.info("Creating new user: " + user.getUsername());
            
            // Hash password before saving
            if (user.getPassword() != null) {
                user.setPassword(hashPassword(user.getPassword()));
            }
            
            // Set creation timestamp
            user.setCreatedDate(LocalDateTime.now());
            user.setUpdatedDate(LocalDateTime.now());
            
            em.persist(user);
            logger.info("User created successfully: " + user.getUsername());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating user: " + user.getUsername(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    // Update existing user
    public void updateUser(User user) {
        try {
            logger.info("Updating user: " + user.getUsername());
            
            // Hash password if it's being updated
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(hashPassword(user.getPassword()));
            }
            
            // Set update timestamp
            user.setUpdatedDate(LocalDateTime.now());
            
            em.merge(user);
            logger.info("User updated successfully: " + user.getUsername());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating user: " + user.getUsername(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    // Delete user
    public void deleteUser(Integer userId) {
        try {
            logger.info("Deleting user with ID: " + userId);
            
            User user = em.find(User.class, userId);
            if (user != null) {
                em.remove(user);
                logger.info("User deleted successfully: " + user.getUsername());
            } else {
                logger.warning("User not found with ID: " + userId);
                throw new RuntimeException("User not found");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting user with ID: " + userId, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    // Find user by ID
    public User findUserById(Integer userId) {
        try {
            return em.find(User.class, userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
            return null;
        }
    }
    
    // Find user by username
    public User findUserByUsername(String username) {
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.info("User not found with username: " + username);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by username: " + username, e);
            return null;
        }
    }
    
    // Find user by email
    public User findUserByEmail(String email) {
        try {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            logger.info("User not found with email: " + email);
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding user by email: " + email, e);
            return null;
        }
    }
    
    // Authenticate user
    public User authenticateUser(String username, String password) {
        try {
            logger.info("Authenticating user: " + username);
            
            User user = findUserByUsername(username);
            if (user != null && verifyPassword(password, user.getPassword())) {
                logger.info("User authenticated successfully: " + username);
                return user;
            }
            
            logger.warning("Authentication failed for user: " + username);
            return null;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error authenticating user: " + username, e);
            return null;
        }
    }
    
    // Search users by keyword (username or email)
    public List<User> searchUsers(String keyword) {
        try {
            logger.info("Searching users with keyword: " + keyword);
            
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(:keyword) " +
                "OR LOWER(u.email) LIKE LOWER(:keyword) ORDER BY u.username", User.class);
            query.setParameter("keyword", "%" + keyword + "%");
            
            List<User> results = query.getResultList();
            logger.info("Search returned " + results.size() + " users");
            return results;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching users with keyword: " + keyword, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
    
    // Get users by role
    public List<User> getUsersByRole(UserRole role) {
        try {
            logger.info("Getting users by role: " + role);
            
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role ORDER BY u.username", User.class);
            query.setParameter("role", role);
            
            List<User> results = query.getResultList();
            logger.info("Found " + results.size() + " users with role " + role);
            return results;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting users by role: " + role, e);
            throw new RuntimeException("Failed to get users by role", e);
        }
    }
    
    // Get user count
    public long getUserCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user count", e);
            return 0;
        }
    }
    
    // Get user count by role
    public long getUserCountByRole(UserRole role) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.role = :role", Long.class);
            query.setParameter("role", role);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user count by role: " + role, e);
            return 0;
        }
    }
    
    // Check if username exists
    public boolean usernameExists(String username) {
        return findUserByUsername(username) != null;
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        return findUserByEmail(email) != null;
    }
    
    // Password hashing utility
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    // Password verification utility
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }
}