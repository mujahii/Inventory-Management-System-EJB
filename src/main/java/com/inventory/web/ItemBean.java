package com.inventory.web;

import com.inventory.ejb.ItemServices;
import com.inventory.entity.Item;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped; // Consider @ViewScoped if the bean's lifecycle should be tied to a single JSF view
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Import LocalDateTime for createdDate/updatedDate
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("itemBean") // Ensure the name matches what's used in XHTML (e.g., #{itemBean.items})
@SessionScoped // Keep SessionScoped if you need state across requests, otherwise ViewScoped might be more appropriate for a single page
public class ItemBean implements Serializable {

    private static final Logger logger = Logger.getLogger(ItemBean.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private ItemServices itemServices; // Inject your EJB for Item operations

    // List to hold all items for display in the dataTable
    private List<Item> items;

    // This 'selectedItem' will now be used consistently for both
    // creating a new item AND editing an existing one in the dialog.
    // The previous separate 'newItem...' and 'editItem...' fields are removed
    // to simplify and unify the dialog's binding.
    private Item selectedItem;

    private boolean editMode = false; // True if editing an existing item, false if adding a new one

    // Search and filter fields
    private String searchKeyword;
    private String filterCategory;
    private String filterStatus;
    private Integer minQuantity;
    private Integer maxQuantity;

    // Filtered items for display
    private List<Item> filteredItems;

    // View mode
    private String viewMode = "grid"; // grid or table

    // User-specific methods for inventory page
    private List<Item> userItems;

    @PostConstruct
    public void init() {
        logger.info("ItemBean initialized");
        loadAllItems();
        // Initialize selectedItem for potential new item creation right away
        // This ensures the dialog has a non-null object to bind to on initial load
        selectedItem = new Item();
    }

    // --- Core Item Management Methods ---

    // Load all items for the current user
    public void loadAllItems() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                items = itemServices.getItemsByUserId(currentUserId);
                logger.info("Loaded " + items.size() + " items for user " + currentUserId);
            } else {
                logger.warning("No current user found, items list will be empty. Redirecting to login if not already there.");
                // Optionally redirect to login if no user is found, depending on your security flow
                // FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml?faces-redirect=true");
                items = new ArrayList<>();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading items", e);
            addErrorMessage("Failed to load items. " + e.getMessage());
        }
    }

    // Prepares the dialog for adding a new item
    public void prepareNewItem() {
        selectedItem = new Item(); // Create a brand new Item object
        editMode = false; // Set to add mode
        logger.info("Prepared for new item entry. Edit mode: " + editMode);
    }

    // Prepares the dialog for editing an existing item
    public void selectForEdit(Item item) {
        // Create a copy of the item to selectedItem to allow "cancel" to revert changes.
        selectedItem = new Item();
        selectedItem.setItemId(item.getItemId());
        selectedItem.setItemName(item.getItemName());
        selectedItem.setDescription(item.getDescription());
        selectedItem.setQuantity(item.getQuantity());
        selectedItem.setPrice(item.getPrice());
        selectedItem.setCategory(item.getCategory());
        selectedItem.setLocation(item.getLocation());
        selectedItem.setUser(item.getUser()); // Set the User object
        selectedItem.setCreatedDate(item.getCreatedDate());
        selectedItem.setUpdatedDate(item.getUpdatedDate());

        editMode = true; // Set to edit mode
        logger.info("Selected item for edit: " + selectedItem.getItemName() + ". Edit mode: " + editMode);
    }
    // Prepares the dialog for viewing an existing item
    public void selectForView(Item item) {
        // Create a copy of the item to selectedItem to allow "cancel" to revert changes.
        selectedItem = new Item();
        selectedItem.setItemId(item.getItemId());
        selectedItem.setItemName(item.getItemName());
        selectedItem.setDescription(item.getDescription());
        selectedItem.setQuantity(item.getQuantity());
        selectedItem.setPrice(item.getPrice());
        selectedItem.setCategory(item.getCategory());
        selectedItem.setLocation(item.getLocation());
        selectedItem.setUser(item.getUser()); // Set the User object
        selectedItem.setCreatedDate(item.getCreatedDate());
        selectedItem.setUpdatedDate(item.getUpdatedDate());

        editMode = false; // Set to view mode
        logger.info("Selected item for view: " + selectedItem.getItemName() + ". Edit mode: " + editMode);
    }

    // Saves or updates an item based on 'editMode' (determined by selectedItem.getItemId())
    public void saveItem() {
        try {
            if (!validateItemForm(selectedItem)) {
                return;
            }
            com.inventory.entity.User currentUser = getCurrentUser();
            if (currentUser == null) {
                addErrorMessage("User not logged in. Cannot save item.");
                return;
            }
            selectedItem.setUser(currentUser); // Set the User object
            if (selectedItem.getItemId() == null) {
                selectedItem.setCreatedDate(LocalDateTime.now());
                selectedItem.setUpdatedDate(LocalDateTime.now());
                itemServices.createItem(selectedItem);
                addInfoMessage("Item '" + selectedItem.getItemName() + "' added successfully!");
                logger.info("Item created: " + selectedItem.getItemName());
            } else {
                selectedItem.setUpdatedDate(LocalDateTime.now());
                itemServices.updateItem(selectedItem);
                addInfoMessage("Item '" + selectedItem.getItemName() + "' updated successfully!");
                logger.info("Item updated: " + selectedItem.getItemName());
            }
            loadAllItems();
            cancelDialogAndClearSelection();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving item: " + (selectedItem != null ? selectedItem.getItemName() : "null"), e);
            addErrorMessage("Failed to save item: " + e.getMessage());
        }
    }

    // Deletes an item
    public void deleteItem(Item item) {
        try {
            logger.info("Deleting item: " + item.getItemName());
            itemServices.deleteItem(item.getItemId());
            loadAllItems(); // Refresh the list
            addInfoMessage("Item '" + item.getItemName() + "' deleted successfully!");
            logger.info("Item deleted: " + item.getItemName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting item", e);
            addErrorMessage("Failed to delete item: " + e.getMessage());
        }
    }

    // Cancels the dialog and resets the selected item and edit mode
    // Called when dialog is hidden or 'Cancel' button is clicked
    public void cancelDialogAndClearSelection() {
        selectedItem = new Item(); // Reset to a new, empty item
        editMode = false; // Ensure not in edit mode
        logger.info("Dialog cancelled and selection cleared. Edit mode: " + editMode);
    }

    // --- Search and Filter Methods (Legacy) ---

    public void searchItemsLegacy() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                addErrorMessage("User not logged in");
                return;
            }
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                loadAllItems(); // If search is empty, show all items
                return;
            }
            items = itemServices.searchItems(currentUserId, searchKeyword.trim());
            logger.info("Search returned " + items.size() + " items for keyword: " + searchKeyword);
            addInfoMessage("Search completed. Found " + items.size() + " items.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error searching items", e);
            addErrorMessage("Failed to search items: " + e.getMessage());
        }
    }

    public void filterItems() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                addErrorMessage("User not logged in");
                return;
            }
            if (filterCategory == null || filterCategory.trim().isEmpty()) {
                loadAllItems(); // If filter is empty, show all items
                return;
            }
            items = itemServices.getItemsByCategory(currentUserId, filterCategory.trim());
            logger.info("Filter returned " + items.size() + " items in category " + filterCategory);
            addInfoMessage("Category filter applied. Found " + items.size() + " items.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering items by category", e);
            addErrorMessage("Failed to filter items by category: " + e.getMessage());
        }
    }

    public void filterByQuantity() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                addErrorMessage("User not logged in");
                return;
            }
            if (minQuantity == null && maxQuantity == null) {
                loadAllItems(); // If no quantity limits, show all items
                return;
            }
            int min = minQuantity != null ? minQuantity : 0;
            int max = maxQuantity != null ? maxQuantity : Integer.MAX_VALUE;
            items = itemServices.getItemsByQuantityRange(currentUserId, min, max);
            logger.info("Quantity filter returned " + items.size() + " items for range " + min + "-" + max);
            addInfoMessage("Quantity filter applied. Found " + items.size() + " items.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error filtering by quantity", e);
            addErrorMessage("Failed to filter by quantity: " + e.getMessage());
        }
    }

    public void clearSearchAndFilter() {
        searchKeyword = null;
        filterCategory = null;
        minQuantity = null;
        maxQuantity = null;
        loadAllItems(); // Reload all items after clearing filters
        addInfoMessage("Search and filters cleared. Displaying all items.");
    }

    public void showLowStockItems() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                addErrorMessage("User not logged in");
                return;
            }
            items = itemServices.getLowStockItems(currentUserId, 10);
            logger.info("Low stock filter returned " + items.size() + " items.");
            addInfoMessage("Displaying low stock items (Quantity < 10). Found " + items.size() + " items.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting low stock items", e);
            addErrorMessage("Failed to get low stock items: " + e.getMessage());
        }
    }

    // Get unique categories for current user - returns List<String>
    public List<String> getUniqueCategories() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                return itemServices.getUniqueCategories(currentUserId);
            }
            return new ArrayList<>(); // Return empty list if no user
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting unique categories", e);
            return new ArrayList<>();
        }
    }

    // --- Validation Methods ---

    // Unified validation for the item being saved/updated (selectedItem)
    private boolean validateItemForm(Item item) {
        boolean valid = true;
        FacesContext context = FacesContext.getCurrentInstance(); // Get FacesContext for adding messages

        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            context.addMessage("dialogForm:itemName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Item name is required."));
            valid = false;
        } else if (item.getItemName().trim().length() < 2) {
             context.addMessage("dialogForm:itemName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Item name must be at least 2 characters long."));
            valid = false;
        }

        if (item.getQuantity() == null || item.getQuantity() < 0) {
            context.addMessage("dialogForm:itemQuantity", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Quantity must be a non-negative number."));
            valid = false;
        }

        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            context.addMessage("dialogForm:itemPrice", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Price must be a non-negative number."));
            valid = false;
        }

        return valid;
    }

    // --- Utility Methods ---

    private Integer getCurrentUserId() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                Object currentUser = session.getAttribute("currentUser");
                if (currentUser instanceof com.inventory.entity.User) {
                    return ((com.inventory.entity.User) currentUser).getUserId();
                } else {
                    logger.warning("currentUser in session is not an instance of com.inventory.entity.User or is null.");
                    // In a real app, you might redirect to login if currentUser is invalid/missing
                    // FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml?faces-redirect=true");
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting current user ID from session", e);
        }
        return null;
    }

    // Utility to get the current User object from session
    private com.inventory.entity.User getCurrentUser() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                Object currentUser = session.getAttribute("currentUser");
                if (currentUser instanceof com.inventory.entity.User) {
                    return (com.inventory.entity.User) currentUser;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting current user from session", e);
        }
        return null;
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    // --- Statistics Methods ---

    public long getTotalItems() {
        return items != null ? items.size() : 0;
    }

    public long getLowStockCount() {
        return items != null ? items.stream().filter(i -> i.getQuantity() != null && i.getQuantity() < 10).count() : 0;
    }

    public BigDecimal getTotalValue() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
            .filter(item -> item.getPrice() != null && item.getQuantity() != null) // Avoid NullPointer if price/quantity is null
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getUniqueCategoriesCount() {
        if (items == null) return 0;
        return items.stream()
            .map(Item::getCategory)
            .filter(cat -> cat != null && !cat.trim().isEmpty())
            .distinct()
            .count();
    }

    // --- Dashboard statistics methods
    public long getAvailableItems() {
        return items != null ? items.stream().filter(i -> i.getQuantity() != null && i.getQuantity() > 0).count() : 0;
    }

    public long getCategoriesCount() {
        return items != null ? items.stream().map(Item::getCategory).distinct().count() : 0;
    }

    // --- View Utilities ---

    public boolean isLowStock(Item item) {
        return item.getQuantity() != null && item.getQuantity() < 10;
    }

    public String getStockStatus(Item item) {
        if (item.getQuantity() == null) return "Unknown Stock";
        if (item.getQuantity() == 0) return "Out of Stock";
        if (item.getQuantity() < 10) return "Low Stock";
        return "In Stock";
    }

    public String getStockStatusClass(Item item) {
        if (item.getQuantity() == null) return ""; // No specific class for unknown
        if (item.getQuantity() == 0) return "danger";
        if (item.getQuantity() < 10) return "warning";
        return "success";
    }

    // --- Getters and Setters for JSF Binding ---

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Item selectedItem) {
        this.selectedItem = selectedItem;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getFilterCategory() {
        return filterCategory;
    }

    public void setFilterCategory(String filterCategory) {
        this.filterCategory = filterCategory;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }
    
    public List<Item> getFilteredItems() {
        if (filteredItems == null) {
            filteredItems = new ArrayList<>();
            if (items != null) {
                filteredItems.addAll(items);
            }
        }
        return filteredItems;
    }
    
    public void setFilteredItems(List<Item> filteredItems) {
        this.filteredItems = filteredItems;
    }
    
    public List<String> getCategories() {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(Item::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    public List<Item> getUserItems() {
        if (userItems == null) {
            loadUserItems();
        }
        return userItems;
    }
    
    public void setUserItems(List<Item> userItems) {
        this.userItems = userItems;
    }
    
    public void loadUserItems() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                userItems = itemServices.getItemsByUserId(currentUserId);
                logger.info("Loaded " + (userItems != null ? userItems.size() : 0) + " items for user " + currentUserId);
            } else {
                userItems = new ArrayList<>();
                logger.warning("No current user ID found");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading user items", e);
            userItems = new ArrayList<>();
            addErrorMessage("Failed to load your items: " + e.getMessage());
        }
    }
    
    public int getUserItemCount() {
        return getUserItems() != null ? getUserItems().size() : 0;
    }
    
    public int getUserLowStockCount() {
        if (getUserItems() == null) return 0;
        return (int) getUserItems().stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity() <= 10)
                .count();
    }
    
    public void addItem() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                addErrorMessage("User not logged in");
                return;
            }
            
            if (selectedItem == null) {
                selectedItem = new Item();
            }
            
            // Set the user for the item
            selectedItem.setUser(new com.inventory.entity.User());
            selectedItem.getUser().setUserId(currentUserId);
            
            // Save the item
            itemServices.createItem(selectedItem);
            
            addInfoMessage("Item '" + selectedItem.getItemName() + "' added successfully!");
            
            // Refresh the user items list
            loadUserItems();
            
            // Clear the form
            selectedItem = new Item();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding item", e);
            addErrorMessage("Failed to add item: " + e.getMessage());
        }
    }
    
    public void updateItem() {
        try {
            if (selectedItem == null) {
                addErrorMessage("No item selected for update");
                return;
            }
            
            itemServices.updateItem(selectedItem);
            addInfoMessage("Item '" + selectedItem.getItemName() + "' updated successfully!");
            
            // Refresh the user items list
            loadUserItems();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating item", e);
            addErrorMessage("Failed to update item: " + e.getMessage());
        }
    }
    
    public void clearForm() {
        selectedItem = new Item();
        logger.info("Form cleared");
    }
    
    public void searchItems() {
        logger.info("Searching items with keyword: " + searchKeyword + ", category: " + filterCategory + ", status: " + filterStatus);
        
        if (items == null) {
            loadAllItems();
        }
        
        filteredItems = new ArrayList<>();
        
        for (Item item : items) {
            boolean matches = true;
            
            // Search by keyword (name or description)
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String keyword = searchKeyword.toLowerCase().trim();
                String itemName = item.getItemName() != null ? item.getItemName().toLowerCase() : "";
                String itemDescription = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                
                if (!itemName.contains(keyword) && !itemDescription.contains(keyword)) {
                    matches = false;
                }
            }
            
            // Filter by category
            if (matches && filterCategory != null && !filterCategory.trim().isEmpty()) {
                if (!filterCategory.equals(item.getCategory())) {
                    matches = false;
                }
            }
            
            // Filter by status
            if (matches && filterStatus != null && !filterStatus.trim().isEmpty()) {
                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                switch (filterStatus) {
                    case "available":
                        if (quantity <= 0) matches = false;
                        break;
                    case "low_stock":
                        if (quantity > 10) matches = false;
                        break;
                    case "out_of_stock":
                        if (quantity > 0) matches = false;
                        break;
                }
            }
            
            if (matches) {
                filteredItems.add(item);
            }
        }
        
        logger.info("Found " + filteredItems.size() + " items matching criteria");
    }
    
    public void clearFilters() {
        logger.info("Clearing all filters");
        searchKeyword = null;
        filterCategory = null;
        filterStatus = null;
        filteredItems = new ArrayList<>();
        if (items != null) {
            filteredItems.addAll(items);
        }
    }
}