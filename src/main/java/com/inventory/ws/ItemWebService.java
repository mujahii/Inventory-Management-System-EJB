/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.ws;

import com.inventory.ejb.ItemServices;
import com.inventory.entity.Item;
import com.inventory.dto.ItemDTO;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/items")
@Produces(MediaType.APPLICATION_JSON) // All methods will return JSON by default
@Consumes(MediaType.APPLICATION_JSON) // All methods expecting data will accept JSON
public class ItemWebService {

    @EJB
    private ItemServices itemServices;

    private ItemDTO toDTO(Item item) {
        if (item == null) return null;
        ItemDTO dto = new ItemDTO();
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setQuantity(item.getQuantity());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setCategory(item.getCategory());
        dto.setLocation(item.getLocation());
        // Convert Integer userId to Long for DTO
        dto.setUserId(item.getUser() != null && item.getUser().getUserId() != null ? 
                     Long.valueOf(item.getUser().getUserId()) : null);
        dto.setCreatedDate(item.getCreatedDate());
        dto.setUpdatedDate(item.getUpdatedDate());
        return dto;
    }

    private Item toEntity(ItemDTO dto) {
        if (dto == null) return null;
        Item item = new Item();
        item.setItemId(dto.getItemId());
        item.setItemName(dto.getItemName());
        item.setQuantity(dto.getQuantity());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setCategory(dto.getCategory());
        item.setLocation(dto.getLocation());
        // Convert Long userId to Integer for entity
        if (dto.getUserId() != null) {
            com.inventory.entity.User user = new com.inventory.entity.User();
            user.setUserId(dto.getUserId().intValue());
            item.setUser(user);
        } else {
            item.setUser(null);
        }
        item.setCreatedDate(dto.getCreatedDate());
        item.setUpdatedDate(dto.getUpdatedDate());
        return item;
    }

    /**
     * Endpoint to get all items in the system (for an admin).
     * URL: GET /api/items/all
     */
    @GET
    @Path("/all")
    public Response getAllItems() {
        List<Item> items = itemServices.getAllItems();
        List<ItemDTO> dtos = items.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    /**
     * Endpoint to get all items for a specific user.
     * URL: GET /api/items/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    public Response getItemsByUserId(@PathParam("userId") Long userId) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
        }
        try {
            List<Item> items = itemServices.getItemsByUserId(userId.intValue());
            List<ItemDTO> dtos = items.stream().map(this::toDTO).collect(Collectors.toList());
            return Response.ok(dtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                         .entity("Error retrieving items: " + e.getMessage())
                         .build();
        }
    }

    /**
     * Endpoint to get a single item by its ID.
     * URL: GET /api/items/{id}
     */
    @GET
    @Path("/{id}")
    public Response getItemById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Item ID is required").build();
        }
        Item item = itemServices.findItemById(id);
        if (item != null) {
            return Response.ok(toDTO(item)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Item not found").build();
        }
    }

    /**
     * Endpoint to create a new item.
     * URL: POST /api/items
     */
    @POST
    public Response createItem(ItemDTO itemDTO) {
        try {
            if (itemDTO == null || itemDTO.getItemName() == null || itemDTO.getUserId() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Item name and user ID are required").build();
            }
            Item createdItem = itemServices.createItem(toEntity(itemDTO));
            return Response.status(Response.Status.CREATED).entity(toDTO(createdItem)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Endpoint to update an existing item.
     * URL: PUT /api/items/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updateItem(@PathParam("id") Integer id, ItemDTO itemDTO) {
        if (id == null || itemDTO == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Item ID and data are required").build();
        }
        itemDTO.setItemId(id);
        try {
            Item updatedItem = itemServices.updateItem(toEntity(itemDTO));
            return Response.ok(toDTO(updatedItem)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Endpoint to delete an item.
     * URL: DELETE /api/items/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteItem(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Item ID is required").build();
        }
        try {
            itemServices.deleteItem(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}