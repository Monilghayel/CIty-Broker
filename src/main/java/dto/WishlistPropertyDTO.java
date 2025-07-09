package dto;

import entity.Properties;
import entity.PropertyImages;
import java.util.List;

public class WishlistPropertyDTO {
    public Properties property;
    public List<PropertyImages> images;

    // Getter and Setter for property
    public Properties getProperty() {
        return property;
    }

    public void setProperty(Properties property) {
        this.property = property;
    }

    // ✅ Getter and Setter for images (this is what JSF needs!)
    public List<PropertyImages> getImages() {
        return images;
    }

    public void setImages(List<PropertyImages> images) {
        this.images = images;
    }
}
