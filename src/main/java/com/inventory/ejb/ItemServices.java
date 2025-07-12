/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.inventory.ejb;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.inventory.dao.ItemDAO;
import com.inventory.entity.Item;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class ItemServices {
    @Inject
    private ItemDAO itemDAO;

    public void addItem(Item item) {
        itemDAO.save(item);
    }

    public List<Item> getAllItems() {
        return itemDAO.findAll();
    }
    
    private static final Logger logger = Logger.getLogger(ItemServices.class.getName());
    
    @PersistenceContext(unitName = "InventoryPU")
    private EntityManager em;
    
    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * Create a new item
     */
    public Item createItem(Item item) {
        try {
            if (item == null) {
                throw new IllegalArgumentException("Item cannot be null");
            }
            
            // Check if item already exists for this user
            Integer userId = item.getUser() != null ? item.getUser().getUserId() : null;
            if (itemExists(item.getItemName(), userId)) {
                throw new IllegalArgumentException("Item with this name already exists for this user");
            }
            
            em.persist(item);
            em.flush();
            logger.log(Level.INFO, "Item created successfully: {0}", item.getItemName());
            return item;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating item: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create item: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find item by ID
     */
    public Item findItemById(Integer itemId) {
        try {
            if (itemId == null) {
                return null;
            }
            return em.find(Item.class, itemId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding item by ID: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Update an existing item
     */
    public Item updateItem(Item item) {
        try {
            if (item == null || item.getItemId() == null) {
                throw new IllegalArgumentException("Item and item ID cannot be null");
            }
            
            Item existingItem = em.find(Item.class, item.getItemId());
            if (existingItem == null) {
                throw new IllegalArgumentException("Item not found");
            }
            
            // Check if new name conflicts with existing items (excluding current item)
            Integer editUserId = item.getUser() != null ? item.getUser().getUserId() : null;
            if (!existingItem.getItemName().equals(item.getItemName()) && 
                itemExists(item.getItemName(), editUserId)) {
                throw new IllegalArgumentException("Item with this name already exists for this user");
            }
            
            Item updatedItem = em.merge(item);
            em.flush();
            logger.log(Level.INFO, "Item updated successfully: {0}", item.getItemName());
            return updatedItem;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating item: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update item: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete an item
     */
    public void deleteItem(Integer itemId) {
        try {
            if (itemId == null) {
                throw new IllegalArgumentException("Item ID cannot be null");
            }
            
            Item item = em.find(Item.class, itemId);
            if (item != null) {
                em.remove(item);
                em.flush();
                logger.log(Level.INFO, "Item deleted successfully: {0}", item.getItemName());
            } else {
                throw new IllegalArgumentException("Item not found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting item: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete item: " + e.getMessage(), e);
        }
    }
    
    // ==================== USER-SPECIFIC OPERATIONS ====================
    
    /**
     * Get all items for a specific user
     */
    public List<Item> getItemsByUserId(Integer userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            TypedQuery<Item> query = em.createQuery(
                "SELECT i FROM Item i WHERE i.user.userId = :userId ORDER BY i.itemName ASC",
                Item.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting items by user ID: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get items for user: " + e.getMessage(), e);
        }
    }

    /**
     * Search items for a specific user by keyword
     */
    public List<Item> searchItems(Integer userId, String keyword) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            String searchKeyword = keyword != null ? "%" + keyword.toLowerCase() + "%" : "%";
            TypedQuery<Item> query = em.createQuery(
                "SELECT i FROM Item i WHERE i.user.userId = :userId " +
                "AND (LOWER(i.itemName) LIKE :keyword OR LOWER(i.description) LIKE :keyword OR LOWER(i.category) LIKE :keyword) " +
                "ORDER BY i.itemName ASC",
                Item.class
            );
            query.setParameter("userId", userId);
            query.setParameter("keyword", searchKeyword);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching items: " + e.getMessage(), e);
            throw new RuntimeException("Failed to search items: " + e.getMessage(), e);
        }
    }

    /**
     * Get items by category for a specific user
     */
    public List<Item> getItemsByCategory(Integer userId, String category) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (category == null || category.trim().isEmpty()) {
                throw new IllegalArgumentException("Category cannot be null or empty");
            }
            TypedQuery<Item> query = em.createQuery(
                "SELECT i FROM Item i WHERE i.user.userId = :userId AND i.category = :category " +
                "ORDER BY i.itemName ASC",
                Item.class
            );
            query.setParameter("userId", userId);
            query.setParameter("category", category);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting items by category: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get items by category: " + e.getMessage(), e);
        }
    }

    /**
     * Get items within a quantity range for a specific user
     */
    public List<Item> getItemsByQuantityRange(Integer userId, int minQuantity, int maxQuantity) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (minQuantity < 0 || maxQuantity < minQuantity) {
                throw new IllegalArgumentException("Invalid quantity range");
            }
            TypedQuery<Item> query = em.createQuery(
                "SELECT i FROM Item i WHERE i.user.userId = :userId " +
                "AND i.quantity >= :minQuantity AND i.quantity <= :maxQuantity " +
                "ORDER BY i.quantity ASC",
                Item.class
            );
            query.setParameter("userId", userId);
            query.setParameter("minQuantity", minQuantity);
            query.setParameter("maxQuantity", maxQuantity);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting items by quantity range: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get items by quantity range: " + e.getMessage(), e);
        }
    }

    /**
     * Get low stock items for a specific user
     */
    public List<Item> getLowStockItems(Integer userId, int threshold) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (threshold < 0) {
                throw new IllegalArgumentException("Threshold cannot be negative");
            }
            TypedQuery<Item> query = em.createQuery(
                "SELECT i FROM Item i WHERE i.user.userId = :userId AND i.quantity <= :threshold " +
                "ORDER BY i.quantity ASC",
                Item.class
            );
            query.setParameter("userId", userId);
            query.setParameter("threshold", threshold);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting low stock items: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get low stock items: " + e.getMessage(), e);
        }
    }

    /**
     * Get unique categories for a specific user
     */
    public List<String> getUniqueCategories(Integer userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            TypedQuery<String> query = em.createQuery(
                "SELECT DISTINCT i.category FROM Item i WHERE i.user.userId = :userId AND i.category IS NOT NULL " +
                "ORDER BY i.category ASC",
                String.class
            );
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting unique categories: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get unique categories: " + e.getMessage(), e);
        }
    }

    /**
     * Get total number of items for a specific user
     */
    public long getItemCount(Integer userId) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(i) FROM Item i WHERE i.user.userId = :userId",
                Long.class
            );
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting item count: " + e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Get total quantity of all items for a specific user
     */
    public long getTotalQuantity(Integer userId) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(i.quantity), 0) FROM Item i WHERE i.user.userId = :userId",
                Long.class
            );
            query.setParameter("userId", userId);
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total quantity: " + e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Get total value of all items for a user
     */
    public double getTotalValue(Integer userId) {
        try {
            if (userId == null) {
                return 0.0;
            }
            TypedQuery<Double> query = em.createQuery(
                "SELECT COALESCE(SUM(i.quantity * i.price), 0.0) FROM Item i WHERE i.user.userId = :userId",
                Double.class
            );
            query.setParameter("userId", userId);
            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total value: " + e.getMessage(), e);
            return 0.0;
        }
    }
    
    /**
     * Get low stock count for a user
     */
    public long getLowStockCount(Integer userId, int threshold) {
        try {
            if (userId == null) {
                return 0;
            }
            if (threshold < 0) {
                threshold = 5; // Default threshold
            }
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(i) FROM Item i WHERE i.user.userId = :userId AND i.quantity <= :threshold",
                Long.class
            );
            query.setParameter("userId", userId);
            query.setParameter("threshold", threshold);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting low stock count: " + e.getMessage(), e);
            return 0;
        }
    }
    
    // ==================== ADMIN OPERATIONS ====================
    
    /**
     * Get system-wide statistics (admin only)
     */
    public long getTotalSystemItems() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(i) FROM Item i",
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting total system items: " + e.getMessage(), e);
            return 0;
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if an item exists for a specific user
     */
    private boolean itemExists(String itemName, Integer userId) {
        try {
            if (itemName == null || userId == null) {
                return false;
            }
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(i) FROM Item i WHERE LOWER(i.itemName) = LOWER(:itemName) AND i.user.userId = :userId",
                Long.class
            );
            query.setParameter("itemName", itemName.trim());
            query.setParameter("userId", userId);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking item existence: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Update item quantity
     */
    public void updateQuantity(Integer itemId, int newQuantity) {
        try {
            if (itemId == null) {
                throw new IllegalArgumentException("Item ID cannot be null");
            }
            
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            
            Item item = em.find(Item.class, itemId);
            if (item == null) {
                throw new IllegalArgumentException("Item not found");
            }
            
            item.setQuantity(newQuantity);
            em.merge(item);
            em.flush();
            logger.log(Level.INFO, "Item quantity updated: {0} -> {1}", new Object[]{item.getItemName(), newQuantity});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating item quantity: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update item quantity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Batch update items
     */
    public void updateItems(List<Item> items) {
        try {
            if (items == null || items.isEmpty()) {
                return;
            }
            
            for (Item item : items) {
                if (item.getItemId() != null) {
                    em.merge(item);
                }
            }
            em.flush();
            logger.log(Level.INFO, "Batch updated {0} items", items.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in batch update: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update items: " + e.getMessage(), e);
        }
    }
    /**
     * Delete all items for a specific user
     */
    public void deleteItemsByUserId(Integer userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            List<Item> items = getItemsByUserId(userId);
            for (Item item : items) {
                em.remove(item);
            }
            em.flush();
            logger.log(Level.INFO, "Deleted all items for user ID: {0}", userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting items by user ID: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete items for user: " + e.getMessage(), e);
        }
    }
    /**
     * Delete all items in the system (admin only)
     */
    public void deleteAllItems() {
        try {
            em.createQuery("DELETE FROM Item i").executeUpdate();
            em.flush();
            logger.log(Level.INFO, "Deleted all items in the system");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting all items: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete all items: " + e.getMessage(), e);
        }
    }
}