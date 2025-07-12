/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.inventory.dao;

import com.inventory.entity.Item;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ItemDAO {

    @PersistenceContext(unitName = "InventoryPU")
    private EntityManager em;

    public void save(Item item) {
        em.persist(item);
    }

    public List<Item> findAll() {
        return em.createQuery("SELECT i FROM Item i", Item.class).getResultList();
    }

    public Item findById(Integer id) {
        try {
            return em.find(Item.class, id);
        } catch (Exception e) {
            // Log error if needed
            return null;
        }
    }

    public Item update(Item item) {
        try {
            return em.merge(item);
        } catch (Exception e) {
            // Log error if needed
            return null;
        }
    }

    public void delete(Item item) {
        try {
            em.remove(em.contains(item) ? item : em.merge(item));
        } catch (Exception e) {
            // Log error if needed
        }
    }

    public void deleteById(Integer id) {
        Item item = findById(id);
        if (item != null) {
            delete(item);
        }
    }
    public List<Item> findByName(String itemName) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.itemName = :itemName", Item.class)
                .setParameter("itemName", itemName)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByUserId(Integer userId) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.user.userId = :userId", Item.class)
                .setParameter("userId", userId)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByCategory(String category) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.category = :category", Item.class)
                .setParameter("category", category)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
    public List<Item> findByQuantityGreaterThan(Integer quantity) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.quantity > :quantity", Item.class)
                .setParameter("quantity", quantity)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByLocation(String location) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.location = :location", Item.class)
                .setParameter("location", location)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByPriceRange(Double minPrice, Double maxPrice) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.price BETWEEN :minPrice AND :maxPrice", Item.class)
                .setParameter("minPrice", minPrice)
                .setParameter("maxPrice", maxPrice)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByDescription(String description) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.description LIKE :description", Item.class)
                .setParameter("description", "%" + description + "%")
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByCreatedDateRange(String startDate, String endDate) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.createdDate BETWEEN :startDate AND :endDate", Item.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByUpdatedDateRange(String startDate, String endDate) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.updatedDate BETWEEN :startDate AND :endDate", Item.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByItemNameAndCategory(String itemName, String category) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.itemName = :itemName AND i.category = :category", Item.class)
                .setParameter("itemName", itemName)
                .setParameter("category", category)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByItemNameAndLocation(String itemName, String location) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.itemName = :itemName AND i.location = :location", Item.class)
                .setParameter("itemName", itemName)
                .setParameter("location", location)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByItemNameAndPriceRange(String itemName, Double minPrice, Double maxPrice) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.itemName = :itemName AND i.price BETWEEN :minPrice AND :maxPrice", Item.class)
                .setParameter("itemName", itemName)
                .setParameter("minPrice", minPrice)
                .setParameter("maxPrice", maxPrice)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<Item> findByItemNameAndDescription(String itemName, String description) {
        try {
            return em.createQuery("SELECT i FROM Item i WHERE i.itemName = :itemName AND i.description LIKE :description", Item.class)
                .setParameter("itemName", itemName)
                .setParameter("description", "%" + description + "%")
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

}