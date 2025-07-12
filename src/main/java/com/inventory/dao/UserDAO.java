/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.inventory.dao;

import com.inventory.entity.User;
import com.inventory.entity.UserRole;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
@Stateless
public class UserDAO {

    @PersistenceContext(unitName = "InventoryPU")
    private EntityManager em;

    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void save(User user) {
        em.persist(user);
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public User findById(Integer id) {
        try {
            return em.find(User.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    public User update(User user) {
        try {
            return em.merge(user);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(User user) {
        try {
            em.remove(em.contains(user) ? user : em.merge(user));
        } catch (Exception e) {
            // Log error if needed
        }
    }

    public void deleteById(Integer id) {
        User user = findById(id);
        if (user != null) {
            delete(user);
        }
    }

    public List<User> findByRole(UserRole role) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.role = :role", User.class)
                .setParameter("role", role)
                .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean usernameExists(String username) {
        try {
            return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                     .setParameter("username", username)
                     .getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                     .setParameter("username", username)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<User> findByEmailDomain(String domain) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email LIKE :domain", User.class)
                     .setParameter("domain", "%" + domain)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean emailExists(String email) {
        try {
            return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                     .setParameter("email", email)
                     .getSingleResult() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<User> findByUsernameAndEmail(String username, String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.email = :email", User.class)
                     .setParameter("username", username)
                     .setParameter("email", email)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<User> findByUpdatedDateRange(String startDate, String endDate) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.updatedDate BETWEEN :startDate AND :endDate", User.class)
                     .setParameter("startDate", startDate)
                     .setParameter("endDate", endDate)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<User> findByUsernameAndRole(String username, UserRole role) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.role = :role", User.class)
                     .setParameter("username", username)
                     .setParameter("role", role)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<User> findByEmailAndRole(String email, UserRole role) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.role = :role", User.class)
                     .setParameter("email", email)
                     .setParameter("role", role)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<User> findByUsernameAndEmailAndRole(String username, String email, UserRole role) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.email = :email AND u.role = :role", User.class)
                     .setParameter("username", username)
                     .setParameter("email", email)
                     .setParameter("role", role)
                     .getResultList();
        } catch (Exception e) {
            return List.of();
        }
    }
}