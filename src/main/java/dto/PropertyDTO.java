package dto;

import entity.Properties;
import java.math.BigDecimal;
import java.util.List;

public class PropertyDTO {
    public Integer id;  // Add this if you need to route to property-details.xhtml?id=...
    public Integer seller_id;
    public String seller_name;
    public String seller_email;
    
    public String title;
    public String description;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public String address;
    public String type;
    public Integer bedrooms;
    public Integer bathrooms;
    public Integer area_sqrt;
    public String status;
    public Long price;
    
    private Properties property;
    public List<String> imageLinks;  // Add this to include image URLs
    public Properties getProperty() { return property; }
    public void setProperty(Properties property) { this.property = property; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getSeller_id() { return seller_id; }
    public void setSeller_id(Integer seller_id) { this.seller_id = seller_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    public Integer getArea_sqrt() { return area_sqrt; }
    public void setArea_sqrt(Integer area_sqrt) { this.area_sqrt = area_sqrt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
    public List<String> getImageLinks() { return imageLinks; }
    public void setImageLinks(List<String> imageLinks) { this.imageLinks = imageLinks; }
}
