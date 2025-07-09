package api;

import dto.WishlistDTO;
import dto.WishlistPropertyDTO;
import entity.Properties;
import entity.PropertyImages;
import entity.Users;
import entity.Wishlist;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

@Stateless
@Path("wishlist")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WishlistService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public List<Wishlist> getAllWishlists() {
        return em.createNamedQuery("Wishlist.findAll", Wishlist.class).getResultList();
    }

    @GET
    @Path("{id}")
    public Wishlist getWishlist(@PathParam("id") Integer id) {
        return em.find(Wishlist.class, id);
    }

    @POST
    public Response createWishlist(WishlistDTO dto) {
        Users user = em.find(Users.class, dto.user_id);
        Properties property = em.find(Properties.class, dto.property_id);

        if (user == null || property == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user_id or property_id").build();
        }

        // Prevent duplicate wishlist entries
        List<Wishlist> existing = em.createQuery("SELECT w FROM Wishlist w WHERE w.userId = :user AND w.propertyId = :property", Wishlist.class)
                .setParameter("user", user)
                .setParameter("property", property)
                .getResultList();

        if (!existing.isEmpty()) {
            return Response.status(Response.Status.CONFLICT).entity("Already in wishlist").build();
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(user);
        wishlist.setPropertyId(property);
        wishlist.setCreatedAt(new Date());

        em.persist(wishlist);
        return Response.status(Response.Status.CREATED).entity(wishlist).build();
    }

    @PUT
    @Path("{id}")
    public Response updateWishlist(@PathParam("id") Integer id, Wishlist updatedWishlist) {
        Wishlist existing = em.find(Wishlist.class, id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        existing.setUserId(updatedWishlist.getUserId());
        existing.setPropertyId(updatedWishlist.getPropertyId());
        em.merge(existing);
        return Response.ok(existing).build();
    }

    @POST
    @Path("by-user-property")
    public Response deleteWishlistByUserAndProperty(WishlistDTO dto) {
        Users user = em.find(Users.class, dto.user_id);
        Properties property = em.find(Properties.class, dto.property_id);

        if (user == null || property == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user_id or property_id").build();
        }

        List<Wishlist> existing = em.createQuery(
            "SELECT w FROM Wishlist w WHERE w.userId = :user AND w.propertyId = :property", Wishlist.class)
            .setParameter("user", user)
            .setParameter("property", property)
            .getResultList();

        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Wishlist entry not found").build();
        }
        for (Wishlist w : existing) {
            em.remove(w);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("user/{userId}")
    public List<Wishlist> getWishlistByUser(@PathParam("userId") Integer userId) {
        return em.createQuery("SELECT w FROM Wishlist w WHERE w.userId.id = :userId", Wishlist.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
    
    @GET
    @Path("/user/{userId}/properties-with-images")
    public Response getWishlistPropertiesWithImages(@PathParam("userId") Integer userId) {
        List<Wishlist> wishlist = em.createQuery(
                "SELECT w FROM Wishlist w WHERE w.userId.id = :userId", Wishlist.class)
                .setParameter("userId", userId)
                .getResultList();

        List<WishlistPropertyDTO> result = new ArrayList<>();

        for (Wishlist w : wishlist) {
            WishlistPropertyDTO dto = new WishlistPropertyDTO();

            // ✅ Use setter methods instead of direct field access
            dto.setProperty(w.getPropertyId());

            List<PropertyImages> images = em.createQuery(
                    "SELECT i FROM PropertyImages i WHERE i.propertyId.id = :propertyId", PropertyImages.class)
                    .setParameter("propertyId", w.getPropertyId().getId())
                    .getResultList();
            dto.setImages(images);

            result.add(dto);
        }

        return Response.ok(result).build();
    }


}