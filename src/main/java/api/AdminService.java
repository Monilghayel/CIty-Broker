package api;

import dto.UserDTO;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    // Get all users except admins
    @GET
    @Path("/users")
    public Response getAllNonAdminUsers() {
        List<Users> users = em.createQuery(
                "SELECT u FROM Users u WHERE u.role <> 'admin'", Users.class
        ).getResultList();

        List<UserDTO> dtoList = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Response.ok(dtoList).build();
    }

    // Filter users based on their role (buyer/seller)
    @GET
    @Path("/users/role/{role}")
    public Response getUsersByRole(@PathParam("role") String role) {
        if (!role.equalsIgnoreCase("buyer") && !role.equalsIgnoreCase("seller")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Role must be 'buyer' or 'seller'.")
                    .build();
        }

        List<Users> users = em.createQuery(
                "SELECT u FROM Users u WHERE u.role = :role", Users.class
        ).setParameter("role", role.toLowerCase())
         .getResultList();

        List<UserDTO> dtoList = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return Response.ok(dtoList).build();
    }

    // Count approved bookings for a seller
    @GET
    @Path("/seller/{sellerId}/approved-bookings/count")
    public Response countApprovedBookingsForSeller(@PathParam("sellerId") int sellerId) {
        try {
            Long countResult = em.createQuery(
                    "SELECT COUNT(b) FROM Bookings b WHERE b.propertyId.sellerId.id = :sellerId AND b.status = 'approved'",
                    Long.class)
                    .setParameter("sellerId", sellerId)
                    .getSingleResult();

            int count = countResult.intValue();

            JsonObject json = Json.createObjectBuilder()
                    .add("sellerId", sellerId)
                    .add("approvedBookings", count)
                    .build();

            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error counting approved bookings: " + e.getMessage())
                    .build();
        }
    }

    // ✅ NEW: Count properties for a seller
    @GET
    @Path("/seller/{sellerId}/properties/count")
    public Response countPropertiesForSeller(@PathParam("sellerId") int sellerId) {
        try {
            Long countResult = em.createQuery(
                    "SELECT COUNT(p) FROM Properties p WHERE p.sellerId.id = :sellerId", Long.class)
                    .setParameter("sellerId", sellerId)
                    .getSingleResult();

            int count = countResult.intValue();

            JsonObject json = Json.createObjectBuilder()
                    .add("sellerId", sellerId)
                    .add("propertyCount", count)
                    .build();

            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error counting properties: " + e.getMessage())
                    .build();
        }
    }

    private UserDTO toDTO(Users u) {
        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt());
    }
}
