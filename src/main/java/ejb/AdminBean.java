package ejb;

import dto.UserDTO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Named("adminBean")
@SessionScoped
public class AdminBean implements Serializable {

    private List<UserDTO> userList;
    private String selectedRole;
    private int selectedSellerId;
    private int approvedBookingsCount;

    private List<UserDTO> sellers;
    private final Map<Integer, Integer> approvedBookingCounts = new HashMap<>();
    private final Map<Integer, Integer> propertyCounts = new HashMap<>();

    private UserDTO selectedProfile;
    private final String BASE_URL = "http://localhost:9000/RealEstate_V1/api/admin";

    @PostConstruct
    public void init() {
        fetchSellersWithBookingCounts();
        fetchPropertyCounts();
    }

    public void fetchAllNonAdminUsers() {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(BASE_URL + "/users");
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 200) {
                userList = response.readEntity(new GenericType<List<UserDTO>>() {});
            }
        } finally {
            client.close();
        }
    }

    public void fetchUsersByRole() {
        if (selectedRole == null || selectedRole.isEmpty()) return;
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(BASE_URL + "/users/role/" + selectedRole);
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 200) {
                userList = response.readEntity(new GenericType<List<UserDTO>>() {});
            }
        } finally {
            client.close();
        }
    }

    public void fetchApprovedBookingCount() {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(BASE_URL + "/seller/" + selectedSellerId + "/approved-bookings/count");
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 200) {
                JsonObject json = response.readEntity(JsonObject.class);
                approvedBookingsCount = json.getInt("approvedBookings");
            }
        } finally {
            client.close();
        }
    }

    public void fetchSellersWithBookingCounts() {
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(BASE_URL + "/users/role/seller");
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 200) {
                sellers = response.readEntity(new GenericType<List<UserDTO>>() {});
                approvedBookingCounts.clear();
                for (UserDTO seller : sellers) {
                    int sellerId = seller.getId();
                    WebTarget countTarget = client.target(BASE_URL + "/seller/" + sellerId + "/approved-bookings/count");
                    Response countResponse = countTarget.request().get();
                    if (countResponse.getStatus() == 200) {
                        JsonObject json = countResponse.readEntity(JsonObject.class);
                        approvedBookingCounts.put(sellerId, json.getInt("approvedBookings"));
                    } else {
                        approvedBookingCounts.put(sellerId, 0);
                    }
                }
            }
        } finally {
            client.close();
        }
    }

    public void fetchPropertyCounts() {
        if (sellers == null) return;
        Client client = ClientBuilder.newClient();
        try {
            propertyCounts.clear();
            for (UserDTO seller : sellers) {
                int sellerId = seller.getId();
                WebTarget countTarget = client.target(BASE_URL + "/seller/" + sellerId + "/properties/count");
                Response countResponse = countTarget.request().get();
                if (countResponse.getStatus() == 200) {
                    JsonObject json = countResponse.readEntity(JsonObject.class);
                    propertyCounts.put(sellerId, json.getInt("propertyCount"));
                } else {
                    propertyCounts.put(sellerId, 0);
                }
            }
        } finally {
            client.close();
        }
    }
    public String viewSellerProperties(int sellerId) {
        this.selectedSellerId = sellerId; // store seller ID in session scope
        return "seller-properties?faces-redirect=true"; // navigate to page
    }

    // Navigation
    public String goToProfile(UserDTO user) {
        this.selectedProfile = user;
        return "/agent-profile.xhtml?faces-redirect=true";
    }

    // Getters and Setters (one-liners)
    public UserDTO getSelectedProfile() { return selectedProfile; }
    public List<UserDTO> getUserList() { return userList; }
    public void setUserList(List<UserDTO> userList) { this.userList = userList; }
    public String getSelectedRole() { return selectedRole; }
    public void setSelectedRole(String selectedRole) { this.selectedRole = selectedRole; }
    public int getSelectedSellerId() { return selectedSellerId; }
    public void setSelectedSellerId(int selectedSellerId) { this.selectedSellerId = selectedSellerId; }
    public int getApprovedBookingsCount() { return approvedBookingsCount; }
    public void setApprovedBookingsCount(int approvedBookingsCount) { this.approvedBookingsCount = approvedBookingsCount; }
    public List<UserDTO> getSellers() { return sellers; }
    public Map<Integer, Integer> getApprovedBookingCounts() { return approvedBookingCounts; }
    public Map<Integer, Integer> getPropertyCounts() { return propertyCounts; }
    public int getApprovedBookings(int sellerId) { return approvedBookingCounts.getOrDefault(sellerId, 0); }
    public int getPropertyCount(int sellerId) { return propertyCounts.getOrDefault(sellerId, 0); }

    public BigDecimal getPendingAmountForUser(Integer userId) {
        try {
            // Get total charges (approved bookings * 100)
            int approvedCount = approvedBookingCounts.getOrDefault(userId, 0);
            BigDecimal totalCharges = new BigDecimal(approvedCount * 100);
            
            // Get total transactions for this user
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/transactions/user/" + userId);
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            
            BigDecimal totalPaid = BigDecimal.ZERO;
            if (response.getStatus() == 200) {
                String transactionsJson = response.readEntity(String.class);
                // Parse the JSON array and sum up all transaction amounts
                // For simplicity, we'll use a basic approach
                // In a real application, you might want to use a proper JSON parser
                if (transactionsJson.contains("\"amount\":")) {
                    // This is a simplified approach - in production, use proper JSON parsing
                    String[] parts = transactionsJson.split("\"amount\":");
                    for (int i = 1; i < parts.length; i++) {
                        String amountPart = parts[i].split(",")[0];
                        try {
                            totalPaid = totalPaid.add(new BigDecimal(amountPart));
                        } catch (NumberFormatException e) {
                            // Skip invalid amounts
                        }
                    }
                }
            }
            
            response.close();
            client.close();
            
            // Calculate pending amount
            BigDecimal pendingAmount = totalCharges.subtract(totalPaid);
            return pendingAmount.compareTo(BigDecimal.ZERO) > 0 ? pendingAmount : BigDecimal.ZERO;
            
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
}
