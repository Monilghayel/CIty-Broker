package ejb;

import entity.PropertyImages;
import entity.Properties;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named("propertyImagesBean")
@SessionScoped
public class PropertyImagesBean implements Serializable {

    private final String apiUrl = "http://localhost:9000/RealEstate_V1/api/property_images";
    private PropertyImages image = new PropertyImages();
    private List<PropertyImages> imageList;

    public List<PropertyImages> getImageList() {
        Client client = ClientBuilder.newClient();
        imageList = Arrays.asList(
            client.target(apiUrl)
                .request(MediaType.APPLICATION_JSON)
                .get(PropertyImages[].class)
        );
        return imageList;
    }

    public String addImage() {
        Client client = ClientBuilder.newClient();
        client.target(apiUrl)
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.json(image));
        image = new PropertyImages(); // Reset form
        return "property_images.xhtml?faces-redirect=true";
    }

    public String deleteImage(int id) {
        Client client = ClientBuilder.newClient();
        client.target(apiUrl + "/" + id)
              .request()
              .delete();
        return "property_images.xhtml?faces-redirect=true";
    }

    // Getters and setters
    public PropertyImages getImage() {
        return image;
    }

    public void setImage(PropertyImages image) {
        this.image = image;
    }
}
