package dto;

import java.io.Serializable;
import java.util.Date;

public class ReviewDTO implements Serializable {
    
    private Integer id;
    private Integer rating;
    private String comment;
    private Date createdAt;
    private Integer userId;
    private Integer propertyId;

    public ReviewDTO() {
    }

    public ReviewDTO(Integer rating, String comment, Integer userId, Integer propertyId) {
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.propertyId = propertyId;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }
} 