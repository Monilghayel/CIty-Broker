package api;

import dto.BookingDTO;
import dto.BookingFlatDTO;
import dto.BookingUpdateDTO;
import dto.WishlistDTO;
import entity.Bookings;
import entity.Properties;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
@Path("bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingsService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public List<Bookings> getAllBookings() {
        return em.createNamedQuery("Bookings.findAll", Bookings.class).getResultList();
    }

    @GET
    @Path("{id}")
    public Bookings getBooking(@PathParam("id") Integer id) {
        return em.find(Bookings.class, id);
    }

    @POST
    public Response createBooking(BookingFlatDTO dto) {
        Users user = em.find(Users.class, dto.getUser_id());
        Properties property = em.find(Properties.class, dto.getProperty_id());

        if (user == null || property == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user or property").build();
        }

        Bookings booking = new Bookings();
        booking.setUserId(user);
        booking.setPropertyId(property);
        booking.setDate(dto.getDate());
        booking.setTime(dto.getTime());
        booking.setStatus("pending");

        em.persist(booking);
        return Response.status(Response.Status.CREATED).entity(booking).build();
    }

    @PUT
    @Path("{id}")
    public Response updateBooking(@PathParam("id") Integer id, Bookings updated) {
        Bookings existing = em.find(Bookings.class, id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        existing.setDate(updated.getDate());
        existing.setStatus(updated.getStatus());
        existing.setPropertyId(updated.getPropertyId());
        existing.setUserId(updated.getUserId());
        em.merge(existing);
        return Response.ok(existing).build();
    }

    @PUT
    @Path("update-by-user-property")
    public Response updateBookingByUserAndProperty(BookingUpdateDTO dto) {
        Users user = em.find(Users.class, dto.getUser_id());
        Properties property = em.find(Properties.class, dto.getProperty_id());

        if (user == null || property == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid user_id or property_id").build();
        }

        List<Bookings> existingList = em.createQuery(
                "SELECT b FROM Bookings b WHERE b.userId = :user AND b.propertyId = :property", Bookings.class)
                .setParameter("user", user)
                .setParameter("property", property)
                .getResultList();

        if (existingList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Booking not found").build();
        }

        // Assuming only one booking per user-property pair
        Bookings booking = existingList.get(0);

        if (dto.getDate() != null) {
            booking.setDate(dto.getDate());
        }
        if (dto.getTime() != null) {
            booking.setTime(dto.getTime());
        }
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            booking.setStatus(dto.getStatus());
        }

        em.merge(booking);
        return Response.ok(booking).build();
    }

    @POST
    @Path("by-user-property")
    public Response deleteBookingByUserAndProperty(WishlistDTO dto) {
        Users user = em.find(Users.class, dto.user_id);
        Properties property = em.find(Properties.class, dto.property_id);
        if (user == null || property == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid user_id or property_id").build();
        }
        List<Bookings> existing = em.createQuery(
                "SELECT b FROM Bookings b WHERE b.userId = :user AND b.propertyId = :property", Bookings.class)
                .setParameter("user", user)
                .setParameter("property", property)
                .getResultList();

        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Booking not found").build();
        }
        for (Bookings b : existing) {
            em.remove(b);
        }
        return Response.noContent().build();
    }

//  http://localhost:9000/RealEstate_V1/api/bookings/check?user_id=10&property_id=4
    @GET
    @Path("/check")
    public boolean checkBookingExists(@QueryParam("user_id") int userId,
            @QueryParam("property_id") int propertyId) {
        List<Bookings> existing = em.createQuery("SELECT b FROM Bookings b WHERE b.userId.id = :userId AND b.propertyId.id = :propertyId", Bookings.class)
                .setParameter("userId", userId)
                .setParameter("propertyId", propertyId)
                .getResultList();
        return !existing.isEmpty();
    }

    @DELETE
    @Path("{id}")
    public Response deleteBooking(@PathParam("id") Integer id) {
        Bookings booking = em.find(Bookings.class, id);
        if (booking != null) {
            em.remove(booking);
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("user/{userId}")
    public Response getBookingsByUser(@PathParam("userId") Integer userId) {
        try {
            List<Bookings> bookings = em.createQuery(
                    "SELECT b FROM Bookings b WHERE b.userId.id = :userId", Bookings.class)
                    .setParameter("userId", userId)
                    .getResultList();

            List<BookingDTO> dtoList = new ArrayList<>();

            for (Bookings b : bookings) {
                BookingDTO dto = new BookingDTO();

                // Basic booking info
                dto.setUserId(b.getUserId().getId());
                dto.setPropertyId(b.getPropertyId().getId());
                dto.setDate(b.getDate());
                dto.setTime(b.getTime());
                dto.setStatus(b.getStatus());

                // Property details
                Properties property = b.getPropertyId();
                dto.setPropertyTitle(property.getTitle());
                dto.setPropertyAddress(property.getAddress());
                dto.setPropertyPrice(property.getPrice());
                dto.setAddress(property.getAddress());

                Users seller = property.getSellerId();
                dto.setSellerName(seller.getName());
                dto.setSellerEmail(seller.getEmail());

                // Property images
                dto.setPropertyImages(
                        property.getPropertyImagesCollection()
                                .stream()
                                .map(img -> img.getImg())
                                .toList()
                );

                dtoList.add(dto);
            }

            return Response.ok(dtoList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving bookings.").build();
        }
    }

    
@GET
@Path("/filter")
public List<Bookings> filterBookings(@QueryParam("userName") String userName,
                                     @QueryParam("sellerName") String sellerName,
                                     @QueryParam("date") String dateString,
                                     @QueryParam("propertyName") String propertyName,
                                     @QueryParam("propertyType") String propertyType,
                                     @QueryParam("status") String status) {

    String jpql = "SELECT b FROM Bookings b WHERE 1=1";

    if (userName != null && !userName.isEmpty()) {
        jpql += " AND LOWER(b.userId.name) LIKE LOWER(CONCAT('%', :userName, '%'))";
    }
    if (sellerName != null && !sellerName.isEmpty()) {
        jpql += " AND LOWER(b.propertyId.sellerId.name) LIKE LOWER(CONCAT('%', :sellerName, '%'))";
    }
    if (dateString != null && !dateString.isEmpty()) {
        jpql += " AND FUNCTION('DATE', b.date) = :date";
    }
    if (propertyName != null && !propertyName.isEmpty()) {
        jpql += " AND LOWER(b.propertyId.title) LIKE LOWER(CONCAT('%', :propertyName, '%'))";
    }
    if (propertyType != null && !propertyType.isEmpty()) {
        jpql += " AND LOWER(b.propertyId.type) LIKE LOWER(CONCAT('%', :propertyType, '%'))";
    }
    if (status != null && !status.isEmpty()) {
        jpql += " AND LOWER(b.status) LIKE LOWER(CONCAT('%', :status, '%'))";
    }

    TypedQuery<Bookings> query = em.createQuery(jpql, Bookings.class);

    if (userName != null && !userName.isEmpty()) {
        query.setParameter("userName", userName);
    }
    if (sellerName != null && !sellerName.isEmpty()) {
        query.setParameter("sellerName", sellerName);
    }
    if (dateString != null && !dateString.isEmpty()) {
        try {
//            java.sql.Date date = java.sql.Date.valueOf(dateString); // Parses yyyy-MM-dd
            query.setParameter("date", dateString);
        } catch (IllegalArgumentException e) {
            return List.of(); // Or handle invalid format appropriately
        }
    }
    if (propertyName != null && !propertyName.isEmpty()) {
        query.setParameter("propertyName", propertyName);
    }
    if (propertyType != null && !propertyType.isEmpty()) {
        query.setParameter("propertyType", propertyType);
    }
    if (status != null && !status.isEmpty()) {
        query.setParameter("status", status);
    }

    return query.getResultList();
}




    @GET
    @Path("appointment-request/{sellerId}")
    public Response getBookingsBySeller(@PathParam("sellerId") Integer sellerId) {
        try {
            List<Bookings> bookings = em.createQuery(
                    "SELECT b FROM Bookings b WHERE b.propertyId.sellerId.id = :sellerId", Bookings.class)
                    .setParameter("sellerId", sellerId)
                    .getResultList();

            List<BookingDTO> dtoList = new ArrayList<>();

            for (Bookings b : bookings) {
                BookingDTO dto = new BookingDTO();

                // Basic booking info
                dto.setUserId(b.getUserId().getId());
                dto.setPropertyId(b.getPropertyId().getId());
                dto.setDate(b.getDate());
                dto.setTime(b.getTime());
                dto.setStatus(b.getStatus());

                // Property details
                Properties property = b.getPropertyId();
                dto.setPropertyTitle(property.getTitle());
                dto.setPropertyAddress(property.getAddress());
                dto.setPropertyPrice(property.getPrice());
                dto.setAddress(property.getAddress());

//                Users user = new Users();
                dto.setUserName(b.getUserId().getName());
                dto.setUserEmail(b.getUserId().getEmail());

                // Property images
                dto.setPropertyImages(
                        property.getPropertyImagesCollection()
                                .stream()
                                .map(img -> img.getImg())
                                .toList()
                );

                dtoList.add(dto);
            }

            return Response.ok(dtoList).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving bookings by seller.").build();
        }
    }

}
