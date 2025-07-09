package api;

import dto.UserLoginRequest;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.persistence.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import utils.JWTUtil;

import java.util.Date;
import java.util.List;

@Stateless
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @POST
    @Path("/login")
    public Response login(UserLoginRequest loginRequest) {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class);
            query.setParameter("email", loginRequest.getEmail());

            Users user = query.getSingleResult();

            if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                String token = JWTUtil.generateToken(user.getEmail(), user.getRole());

                return Response.ok(Json.createObjectBuilder()
                        .add("token", token)
                        .build())
                        .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Json.createObjectBuilder()
                                .add("error", "Invalid credentials")
                                .build())
                        .build();
            }

        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                            .add("error", "Invalid credentials")
                            .build())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Json.createObjectBuilder()
                            .add("error", "Login error: " + e.getMessage())
                            .build())
                    .build();
        }
    }

    @GET
    @Path("/me")
    public Response getMe(@CookieParam("token") Cookie tokenCookie,
                          @HeaderParam("Authorization") String authHeader) {
        String token = null;

        if (tokenCookie != null) {
            token = tokenCookie.getValue();
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null || token.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Authorization token is missing").build();
        }

        try {
            String email = JWTUtil.getEmailFromToken(token);
            if (email == null || email.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid token").build();
            }

            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class);
            query.setParameter("email", email);

            Users user = query.getSingleResult();
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User not found").build();
            }

            return Response.ok(user).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired token: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("email/{email}")
    public Response getUserByEmail(@PathParam("email") String email) {
        try {
            Users user = em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Response.ok(user).build();
        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    public Response getAllUsers() {
        List<Users> users = em.createQuery("SELECT u FROM Users u", Users.class).getResultList();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") Long id) {
        Users user = em.find(Users.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with ID " + id + " not found.").build();
        }
        return Response.ok(user).build();
    }

    @POST
    public Response createUser(Users user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("buyer");
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(new Date());
        }

        try {
            em.persist(user);
            return Response.status(Response.Status.CREATED)
                    .entity("User created successfully.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error creating user: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") int id, Users updatedUser) {
        Users user = em.find(Users.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with ID " + id + " not found.").build();
        }

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null) {
            user.setPassword(updatedUser.getPassword());
        }

        em.merge(user);
        return Response.ok("User updated successfully.").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) {
        Users user = em.find(Users.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with ID " + id + " not found.").build();
        }

        em.remove(user);
        return Response.ok("User deleted successfully.").build();
    }
}
