/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inventory.ws;

import com.inventory.ejb.UserServices;
import com.inventory.entity.User;
import com.inventory.dto.UserDTO;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserWebService {

    @EJB
    private UserServices userServices;

    private UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setUpdatedDate(user.getUpdatedDate());
        return dto;
    }

    private User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setCreatedDate(dto.getCreatedDate());
        user.setUpdatedDate(dto.getUpdatedDate());
        return user;
    }

    /**
     * Endpoint to get all users.
     * URL: GET /api/users
     */
    @GET
    public Response getAllUsers() {
        List<User> users = userServices.getAllUsers();
        List<UserDTO> dtos = users.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    /**
     * Endpoint to get a single user by ID.
     * URL: GET /api/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
        }
        User user = userServices.findUserById(id);
        if (user != null) {
            return Response.ok(toDTO(user)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }
    }

    /**
     * Endpoint to create (register) a new user.
     * URL: POST /api/users/register
     */
    @POST
    @Path("/register")
    public Response registerUser(UserDTO userDTO) {
        try {
            if (userDTO == null || userDTO.getUsername() == null || userDTO.getEmail() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Username and email are required").build();
            }
            if (userServices.usernameExists(userDTO.getUsername())) {
                return Response.status(Response.Status.CONFLICT).entity("Username already exists.").build();
            }
            if (userServices.emailExists(userDTO.getEmail())) {
                return Response.status(Response.Status.CONFLICT).entity("Email already exists.").build();
            }
            // Password should be set and hashed in the service layer
            User user = toEntity(userDTO);
            userServices.createUser(user);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    /**
     * Endpoint for user authentication.
     * URL: POST /api/users/login
     */
    @POST
    @Path("/login")
    public Response login(UserDTO credentials) {
        if (credentials == null || credentials.getUsername() == null || credentials.getEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Username/email is required").build();
        }
        // Password should be handled securely in the service layer
        User authenticatedUser = userServices.authenticateUser(credentials.getUsername(), credentials.getEmail());
        if (authenticatedUser != null) {
            return Response.ok(toDTO(authenticatedUser)).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password.").build();
        }
    }
}