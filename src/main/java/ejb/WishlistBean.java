    package ejb;

import dto.PropertyDTO;
    import dto.WishlistDTO;
import dto.WishlistPropertyDTO;
    import entity.Properties;
    import entity.Users;
    import entity.Wishlist;
    import jakarta.annotation.PostConstruct;
    import jakarta.enterprise.context.SessionScoped;
    import jakarta.faces.context.FacesContext;
    import jakarta.inject.Named;
    import jakarta.ws.rs.client.Client;
    import jakarta.ws.rs.client.ClientBuilder;
    import jakarta.ws.rs.client.Entity;
    import jakarta.ws.rs.core.GenericType;
    import jakarta.ws.rs.core.MediaType;
    import jakarta.ws.rs.core.Response;
    import java.io.IOException;

    import java.io.Serializable;
    import java.util.List;

    @Named("wishlistBean")
    @SessionScoped
    public class WishlistBean implements Serializable {

        private List<Wishlist> wishlistList;
        private Wishlist wishlist = new Wishlist();
        private Users user;
        private Properties property;
        private final String REST_API_URL = "http://localhost:9000/RealEstate_V1/api/wishlist";
        private List<WishlistPropertyDTO> wishlistPropertiesWithImages;

        @PostConstruct
        public void init() {
            fetchAll();
        }

        public void fetchAll() {
            Client client = ClientBuilder.newClient();
            wishlistList = client
                    .target(REST_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Wishlist>>() {});
            client.close();
        }

        public String addToWishlist(int userId, int propertyId) {
//            if (userId == null || propertyId == null) {
//                System.out.println("addToWishlist failed: userId or propertyId is null");
//                return null;
//            }

            Client client = ClientBuilder.newClient();
            WishlistDTO dto = new WishlistDTO();
            dto.user_id = userId;
            dto.property_id = propertyId;

            Response response = client
                    .target(REST_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(dto, MediaType.APPLICATION_JSON));

            System.out.println("Add Wishlist response code: " + response.getStatus());
            System.out.println("Add Wishlist response body: " + response.readEntity(String.class));
            client.close();

            if (response.getStatus() == 201) {
                fetchAll();
            }
            return null;
        }

        
        public String addToWishlist() {
            if (user == null || property == null) {
                System.out.println("addToWishlist failed: user or property is null");
                return null;
            }
            System.out.println("User ID: " + user.getId());
            System.out.println("Property ID: " + property.getId());

            Client client = ClientBuilder.newClient();

            WishlistDTO dto = new WishlistDTO();
            dto.user_id = user.getId();
            dto.property_id = property.getId();

            Response response = client
                    .target(REST_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(dto, MediaType.APPLICATION_JSON));

            System.out.println("Add Wishlist response code: " + response.getStatus());

            client.close();

            if (response.getStatus() == 201) {
                fetchAll();
                try {
                    FacesContext.getCurrentInstance().getExternalContext()
                            .redirect("property-details.xhtml?faces-redirect=true");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        public String removeFromWishlist(int userId, int propertyId) {
            Client client = ClientBuilder.newClient();

            WishlistDTO dto = new WishlistDTO();
            dto.user_id = userId;
            dto.property_id = propertyId;

            Response response = client
                    .target(REST_API_URL + "/by-user-property")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(dto, MediaType.APPLICATION_JSON));

            System.out.println("Remove Wishlist response code: " + response.getStatus());

            client.close();

            if (response.getStatus() == 204) {
                fetchAll(); // refresh local list
            } else {
                System.out.println("Failed to remove from wishlist. Status: " + response.getStatus());
            }

            return null;
        }

        public boolean isInWishlist(int userId, int propertyId) {
            if (wishlistList == null) {
                return false;
            }

            return wishlistList.stream().anyMatch(w ->
                    w.getUserId().getId().equals(userId) &&
                    w.getPropertyId().getId().equals(propertyId)
            );
        }
        
        public boolean isInWishlist(Users user, Properties property) {
            if (user == null || property == null) {
                return false;
            }
            return isInWishlist(user.getId(), property.getId());
        }
        
        public String toggleWishlist(int userId, int propertyId) {
            System.out.println("Toggling wishlist for userId=" + userId + ", propertyId=" + propertyId);

            boolean alreadyInWishlist = isInWishlist(userId, propertyId);
            System.out.println("Already in wishlist? " + alreadyInWishlist);

            if (alreadyInWishlist) {
                return removeFromWishlist(userId, propertyId);
            } else {
                return addToWishlist(userId, propertyId);
            }
        }

        public Wishlist getWishlistEntry(Users user, Properties property) {
            return wishlistList.stream()
                    .filter(w -> w.getUserId().getId().equals(user.getId()) &&
                                 w.getPropertyId().getId().equals(property.getId()))
                    .findFirst().orElse(null);
        }
        
        public void loadWishlistWithImages(int userId) {
            Client client = ClientBuilder.newClient();
            wishlistPropertiesWithImages = client
                .target(REST_API_URL + "/user/" + userId + "/properties-with-images")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<WishlistPropertyDTO>>() {});
            client.close();
        }

        public void prepareWishlist(Users user, Properties property) {
            System.out.println("Preparing wishlist with user: " + user + " and property: " + property);
            this.user = user;
            this.property = property;
        }
        
        public List<WishlistPropertyDTO> getWishlistPropertiesWithImages() {
            return wishlistPropertiesWithImages;
        }

        public List<Wishlist> getWishlistList() {
            return wishlistList;
        }
        public void setWishlistList(List<Wishlist> wishlistList) {
            this.wishlistList = wishlistList;
        }

        public Wishlist getWishlist() {
            return wishlist;
        }
        public void setWishlist(Wishlist wishlist) {
            this.wishlist = wishlist;
        }

        public Users getUser() {
            return user;
        }
        public void setUser(Users user) {
            this.user = user;
        }

        public Properties getProperty() {
            return property;
        }
        public void setProperty(Properties property) {
            this.property = property;
        }
    }