package ejb;

import entity.Properties;
import entity.PropertyImages;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import java.io.Serializable;
import java.util.List;

@Named("propertyDetailsBean")
@SessionScoped
public class PropertyDetailsBean implements Serializable {

    private int id;
    private Properties property;
    private List<PropertyImages> images;
    private Users seller;
    private String selectedImage;

    @Inject
    private ReviewsBean reviewsBean;

    @PostConstruct
    public void initEmpty() {
        // Do nothing here. Real loading happens via f:event -> loadPropertyData()
    }

    // Called by <f:event> from JSF
    public void loadPropertyData() {
        if (id <= 0) {
            System.out.println("Property ID is invalid or not set.");
            return;
        }

        try {
            Client client = ClientBuilder.newClient();

            // Fetch property
            property = client.target("http://localhost:9000/RealEstate_V1/api/properties/" + id)
                    .request(MediaType.APPLICATION_JSON)
                    .get(Properties.class);

            // Fetch images
            images = client.target("http://localhost:9000/RealEstate_V1/api/property-images/property/" + id)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PropertyImages>>() {});

            // Set default image
            if (images != null && !images.isEmpty()) {
                selectedImage = images.get(0).getImg();
            }

            // Set seller
            if (property != null && property.getSellerId() != null) {
                seller = property.getSellerId();
            }

            // Initialize reviews bean with property ID
            if (reviewsBean != null) {
                reviewsBean.setPropertyId(id);
                reviewsBean.fetchReviewsByProperty();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For thumbnail click
    public void changeMainImage(String imgUrl) {
        this.selectedImage = imgUrl;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Properties getProperty() {
        return property;
    }

    public List<PropertyImages> getImages() {
        return images;
    }

    public Users getSeller() {
        return seller;
    }

    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

    public ReviewsBean getReviewsBean() {
        return reviewsBean;
    }
}
