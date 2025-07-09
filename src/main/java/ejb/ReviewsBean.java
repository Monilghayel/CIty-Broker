package ejb;

import dto.ReviewDTO;
import entity.Reviews;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("reviewsBean")
@SessionScoped
public class ReviewsBean implements Serializable {

    private List<Reviews> reviewsList = new ArrayList<>();
    private Reviews newReview = new Reviews();
    private int propertyId;
    private int rating = 5; // Default rating
    private String comment = "";

    @Inject
    private ejb.LoginBean loginBean;

    private final String BASE_URL = "http://localhost:9000/RealEstate_V1/api/reviews";
    private final Client client = ClientBuilder.newClient();

    @PostConstruct
    public void init() {
        if (propertyId > 0) {
            fetchReviewsByProperty();
        }
    }

    public void fetchReviewsByProperty() {
        try {
            reviewsList = client.target(BASE_URL + "/property/" + propertyId)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Reviews>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            reviewsList = new ArrayList<>();
        }
    }

    public String submitReview() {
        if (!loginBean.isLoggedIn()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please login to submit a review."));
            return null;
        }

        Users loggedInUser = loginBean.getLoggedInUser();
        if (!"buyer".equals(loggedInUser.getRole())) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Only buyers can submit reviews."));
            return null;
        }

        if (comment == null || comment.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please enter a comment."));
            return null;
        }

        if (rating < 1 || rating > 5) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Rating must be between 1 and 5."));
            return null;
        }

        try {
            // Create new review DTO
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setRating(rating);
            reviewDTO.setComment(comment.trim());
            reviewDTO.setCreatedAt(new Date());
            reviewDTO.setUserId(loggedInUser.getId());
            reviewDTO.setPropertyId(propertyId);

            // Submit review via REST API
            Response response = client.target(BASE_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(reviewDTO, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 204 || response.getStatus() == 200) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Review submitted successfully!"));
                
                // Reset form
                rating = 5;
                comment = "";
                
                // Refresh reviews list
                fetchReviewsByProperty();
                
                return null; // Stay on same page
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to submit review. Please try again."));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "An error occurred while submitting the review."));
            return null;
        }
    }

    // Getters & Setters
    public List<Reviews> getReviewsList() {
        return reviewsList;
    }

    public Reviews getNewReview() {
        return newReview;
    }

    public void setNewReview(Reviews newReview) {
        this.newReview = newReview;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
