package api;

import dto.ReviewDTO;
import entity.Reviews;
import entity.Properties;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;

@Stateless
@Path("reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewsService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public List<Reviews> getAllReviews() {
        return em.createNamedQuery("Reviews.findAll", Reviews.class).getResultList();
    }

    @GET
    @Path("{id}")
    public Reviews getReview(@PathParam("id") Integer id) {
        return em.find(Reviews.class, id);
    }

    @GET
    @Path("/property/{propertyId}")
    public List<Reviews> getReviewsByProperty(@PathParam("propertyId") Integer propertyId) {
        return em.createQuery("SELECT r FROM Reviews r WHERE r.propertyId.id = :propertyId ORDER BY r.createdAt DESC", Reviews.class)
                 .setParameter("propertyId", propertyId)
                 .getResultList();
    }

    @GET
    @Path("/property/{propertyId}/average-rating")
    public Double getAverageRatingByProperty(@PathParam("propertyId") Integer propertyId) {
        try {
            Double avgRating = (Double) em.createQuery(
                "SELECT AVG(r.rating) FROM Reviews r WHERE r.propertyId.id = :propertyId")
                .setParameter("propertyId", propertyId)
                .getSingleResult();
            return avgRating != null ? avgRating : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @POST
    public void createReview(ReviewDTO reviewDTO) {
        try {
            // Find and set the user
            Users user = em.find(Users.class, reviewDTO.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + reviewDTO.getUserId());
            }
            
            // Find and set the property
            Properties property = em.find(Properties.class, reviewDTO.getPropertyId());
            if (property == null) {
                throw new IllegalArgumentException("Property not found with ID: " + reviewDTO.getPropertyId());
            }
            
            // Create new review entity
            Reviews review = new Reviews();
            review.setRating(reviewDTO.getRating());
            review.setComment(reviewDTO.getComment());
            review.setCreatedAt(reviewDTO.getCreatedAt() != null ? reviewDTO.getCreatedAt() : new Date());
            review.setUserId(user);
            review.setPropertyId(property);
            
            // Persist the review
            em.persist(review);
            em.flush(); // Ensure the review is persisted immediately
        } catch (Exception e) {
            throw new RuntimeException("Failed to create review: " + e.getMessage(), e);
        }
    }

    @DELETE
    @Path("{id}")
    public void deleteReview(@PathParam("id") Integer id) {
        Reviews review = em.find(Reviews.class, id);
        if (review != null) {
            em.remove(review);
        }
    }
}
