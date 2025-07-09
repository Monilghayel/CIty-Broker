package dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BookingDTO {

    private int userId;
    private int propertyId;
    public LocalDate date;
    public LocalTime time;
    private String status;
    private String address;
    
    private String userName;
    private String userEmail;

    // Enriched Fields for Display
    private String sellerName;
    private String sellerEmail;

    private String propertyTitle;
    private String propertyAddress;
    private Long propertyPrice;
    private List<String> propertyImages;

    // Getters and Setters (One-liners)
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getPropertyTitle() { return propertyTitle; }
    public void setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }

    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }

    public Long getPropertyPrice() { return propertyPrice; }
    public void setPropertyPrice(Long propertyPrice) { this.propertyPrice = propertyPrice; }

    public List<String> getPropertyImages() { return propertyImages; }
    public void setPropertyImages(List<String> propertyImages) { this.propertyImages = propertyImages; }
}
