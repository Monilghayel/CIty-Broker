package ejb;

import dto.PropertyDTO;
import entity.Properties;
import entity.PropertyImages;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import utils.ToastUtil;

@Named("propertyBean")
@ViewScoped
public class PropertyBean implements Serializable {

    private String searchQuery;
    private List<PropertyDTO> searchResults;
    private boolean isSearchActive = false;

    // ====== Properties ======
    private Properties selectedProperty = new Properties();
    private List<String> imageLinks = new ArrayList<>();

    private List<PropertyDTO> allProperties = new ArrayList<>();
    private List<PropertyDTO> filteredProperties = new ArrayList<>();
    private List<PropertyDTO> myProperties;
    private int sellerId;
    @Inject
    private AdminBean adminBean;
    private List<PropertyDTO> sellerProperties;

    private Integer propertyId;
    private String selectedType;
    private Integer minPrice;
    private Integer maxPrice;
    @Inject
    private ToastUtil toastUtil;
    @Inject
    private LoginBean logibBean;

    // ====== Initialization ======
    @PostConstruct
    public void init() {
        if (imageLinks.isEmpty()) {
            imageLinks.add("");
        }
        
        // Initialize lists
        if (allProperties == null) {
            allProperties = new ArrayList<>();
        }
        if (filteredProperties == null) {
            filteredProperties = new ArrayList<>();
        }
        if (myProperties == null) {
            myProperties = new ArrayList<>();
        }
        
        // Fetch data
        fetchAllProperties();
        fetchMyProperties();
        
        // Apply filters
        applyFilters();
    }
    
       public void addImageLink() {
       this.imageLinks.add("");
   }

    public void searchProperties() {
        try {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                isSearchActive = false;
                applyFilters(); // Reset to show all filtered properties
                return;
            }

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/properties/search")
                    .queryParam("query", searchQuery.trim());

            Response response = target.request(MediaType.APPLICATION_JSON).get();

            if (response.getStatus() == 200) {
                searchResults = response.readEntity(new GenericType<List<PropertyDTO>>() {
                });
                isSearchActive = true;
                applyFilters(); // Apply current filters to search results
            } else {
                searchResults = new ArrayList<>();
                isSearchActive = true;
                applyFilters();
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            searchResults = new ArrayList<>();
            isSearchActive = true;
            applyFilters();
        }
    }

    public void clearSearch() {
        searchQuery = null;
        searchResults = null;
        isSearchActive = false;
        applyFilters(); // Reset to show all filtered properties
    }

    public void resetAllFilters() {
        selectedType = null;
        minPrice = null;
        maxPrice = null;
        searchQuery = null;
        searchResults = null;
        isSearchActive = false;
        applyFilters();
    }

    // Getters and Setters
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<PropertyDTO> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<PropertyDTO> searchResults) {
        this.searchResults = searchResults;
    }

    public boolean isSearchActive() {
        return isSearchActive;
    }

    public void setSearchActive(boolean isSearchActive) {
        this.isSearchActive = isSearchActive;
    }

    // ====== Fetching Methods ======
    public void fetchAllProperties() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/properties/with-images");

            Response response = target.request(MediaType.APPLICATION_JSON).get();

            if (response.getStatus() == 200) {
                allProperties = response.readEntity(new GenericType<List<PropertyDTO>>() {
                });
                applyFilters(); // Apply current filters to new data
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchMyProperties() {
        try {
            Users user = logibBean.getLoggedInUser();
            if (user == null) {
                System.out.println("No logged-in user found");
                return;
            }

            int userId = user.getId();
            Client client = ClientBuilder.newClient();
            Response response = client
                    .target("http://localhost:9000/RealEstate_V1/api/properties/seller/" + userId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
//                toastUtil.showSuccess("asds");
                myProperties = response.readEntity(new GenericType<List<PropertyDTO>>() {
                });
            } else {
                System.out.println("Failed to fetch properties: " + response.getStatus());
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchSellerProperties() {
        try {
            int sellerId = adminBean.getSelectedSellerId();

            Client client = ClientBuilder.newClient();
            Response response = client
                    .target("http://localhost:9000/RealEstate_V1/api/properties/seller/" + sellerId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                sellerProperties = response.readEntity(new GenericType<List<PropertyDTO>>() {
                });
            } else {
                System.out.println("Failed to fetch seller properties: " + response.getStatus());
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====== Filter Methods ======
    public void selectTypeAndFetch(String type) {
        this.selectedType = type;
        applyFilters();
    }

    public void applyPriceFilter() {
        // Sync the input values with hidden fields if needed
        applyFilters();
    }

    public void syncPriceInputs() {
        // This method can be called to sync input values
        applyFilters();
    }

    private void applyFilters() {
        // Start with all properties
        List<PropertyDTO> baseList = isSearchActive && searchResults != null ? searchResults : allProperties;

        this.filteredProperties = baseList.stream()
                .filter(p -> {
                    boolean matchesType = (selectedType == null || selectedType.isEmpty()) || selectedType.equalsIgnoreCase(p.getType());
                    boolean matchesMin = (minPrice == null) || p.getPrice() >= minPrice;
                    boolean matchesMax = (maxPrice == null) || p.getPrice() <= maxPrice;
                    return matchesType && matchesMin && matchesMax;
                })
                .collect(Collectors.toList());
    }

    public List<String> getAvailableTypes() {
        return Arrays.asList("apartment", "house", "villa", "commercial", "land");
    }

    // ====== Submit (Add Property and Images) ======
    public void submit() {
        try {
            String token = getTokenFromCookie();
            Users loggedInUser = fetchLoggedInUser(token);
            if (loggedInUser == null) {
                toastUtil.showError("You are not Logged In!");
                return;
            }

            if (selectedProperty.getTitle() == null || selectedProperty.getTitle().trim().isEmpty()) {
                toastUtil.showError("Title is required.");
                return;
            }
            if (selectedProperty.getAddress() == null || selectedProperty.getAddress().trim().isEmpty()) {
                toastUtil.showError("Address is required.");
                return;
            }

            if (selectedProperty.getPrice() < 0) {
                toastUtil.showError("Price cannot be negative.");
                return;
            }
            if (selectedProperty.getPrice() < 10000) {
                toastUtil.showError("Price must be greater than 10,000!");
                return;
            }

            if (selectedProperty.getAreaSqrt() <= 200) {
                toastUtil.showError("Area (sqft) must be greater than 200.");
                return;
            }

            String jsonProperty = String.format(
                    "{ \"seller_id\": %d, \"title\": \"%s\", \"description\": \"%s\", "
                    + "\"latitude\": %s, \"longitude\": %s, \"address\": \"%s\", \"type\": \"%s\", "
                    + "\"bedrooms\": %d, \"bathrooms\": %d, \"area_sqrt\": %s, \"status\": \"%s\", \"price\": %s }",
                    loggedInUser.getId(),
                    selectedProperty.getTitle(),
                    selectedProperty.getDescription(),
                    selectedProperty.getLatitude(),
                    selectedProperty.getLongitude(),
                    selectedProperty.getAddress(),
                    selectedProperty.getType(),
                    selectedProperty.getBedrooms(),
                    selectedProperty.getBathrooms(),
                    selectedProperty.getAreaSqrt(),
                    selectedProperty.getStatus(),
                    selectedProperty.getPrice()
            );

            Client client = ClientBuilder.newClient();
            WebTarget propertyTarget = client.target("http://localhost:9000/RealEstate_V1/api/properties");

            Response propertyResponse = propertyTarget
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(jsonProperty, MediaType.APPLICATION_JSON));

            if (propertyResponse.getStatus() == 201 || propertyResponse.getStatus() == 200) {
                Properties createdProperty = propertyResponse.readEntity(Properties.class);

                List<PropertyImages> images = new ArrayList<>();
                for (String link : imageLinks) {
                    if (link != null && !link.trim().isEmpty()) {
                        PropertyImages img = new PropertyImages();
                        img.setImg(link.trim());
                        img.setPropertyId(createdProperty);
                        img.setCreatedAt(new Date());
                        images.add(img);
                    }
                }

                if (!images.isEmpty()) {
                    WebTarget imageTarget = client.target("http://localhost:9000/RealEstate_V1/api/property-images");

                    List<Map<String, Object>> imageJsonList = new ArrayList<>();
                    for (PropertyImages img : images) {
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("property_id", createdProperty.getId());
                        imageMap.put("img", img.getImg());
                        imageJsonList.add(imageMap);
                    }

                    Response imageResponse = imageTarget
                            .request(MediaType.APPLICATION_JSON)
                            .post(Entity.entity(imageJsonList, MediaType.APPLICATION_JSON));

                    if (imageResponse.getStatus() != 201 && imageResponse.getStatus() != 200) {
                        String errorMsg = imageResponse.readEntity(String.class);
                        toastUtil.showError("Property saved, but image upload failed" + errorMsg);
                    }
                }

                toastUtil.showSuccess("Property added with images!");
                
                // Refresh all property lists to ensure images are displayed immediately
                adminBean.fetchPropertyCounts();
                fetchAllProperties();
                fetchMyProperties();
                
                // Reset form
                selectedProperty = new Properties();
                imageLinks.clear();
                imageLinks.add("");
                
                client.close();
                
                // Redirect to my properties page to show the newly added property
                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("myproperty.xhtml");
                } catch (Exception redirectException) {
                    // If redirect fails, just log it and continue
                    System.out.println("Redirect failed: " + redirectException.getMessage());
                }
                
            } else {
                String error = propertyResponse.readEntity(String.class);
                toastUtil.showError("Failed to add property: " + error);
                client.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            toastUtil.showError("An unexpected error occurred." + e.getMessage());
        }
    }

    public void deleteProperty(int propertyId) {
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:9000/RealEstate_V1/api/properties/" + propertyId)
                .request()
                .delete();

        if (response.getStatus() == 200) {
            fetchMyProperties(); // refresh list
            System.out.println("Property deleted");
        } else {
            toastUtil.showError("Failed to delete property.");
        }
    }

    // ====== Token and User Utility ======
    private String getTokenFromCookie() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Users fetchLoggedInUser(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/users/me");
        Response response = target.request(MediaType.APPLICATION_JSON)
                .cookie(new jakarta.ws.rs.core.Cookie("token", token))
                .get();
        Users user = null;
        if (response.getStatus() == 200) {
            user = response.readEntity(Users.class);
        }
        client.close();
        return user;
    }

    // ====== Getters and Setters ======
    public List<PropertyDTO> getSellerProperties() {
        return sellerProperties;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public Properties getSelectedProperty() {
        return selectedProperty;
    }

    public void setSelectedProperty(Properties selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    public List<String> getImageLinks() {
        return imageLinks;
    }

    public void setImageLinks(List<String> imageLinks) {
        this.imageLinks = imageLinks;
    }

    public List<PropertyDTO> getAllProperties() {
        if (isSearchActive && searchResults != null) {
            return searchResults;
        }
        return filteredProperties;
    }

    public List<PropertyDTO> getMyProperties() {
        return myProperties;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void refreshAllData() {
        fetchAllProperties();
        fetchMyProperties();
        if (adminBean != null) {
            adminBean.fetchPropertyCounts();
        }
    }
}
