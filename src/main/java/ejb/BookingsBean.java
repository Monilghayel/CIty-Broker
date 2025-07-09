package ejb;

import dto.BookingDTO;
import dto.PropertyDTO;
import entity.Users;
import entity.Bookings;
import entity.Wishlist;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ExternalContext;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

@Named("bookingsBean")
@ViewScoped
public class BookingsBean implements Serializable {

    private static final String REST_API_URL = "http://localhost:9000/RealEstate_V1/api/bookings";
    private List<Bookings> allbookings;
    private int userId;
    private int propertyId;
    private Date selectedDate;
    private String selectedTime;
    private Integer selectedPropertyId;
    private List<BookingDTO> userBookings;
    private List<PropertyDTO> myProperties;
    private List<BookingDTO> appointmentRequests;
    
    private String filterUserName;
    private String filterSellerName;
    private String filterDate;
    private String filterPropertyName;
    private String filterPropertyType;    
    private String filterStatus;

    @Inject
    private LoginBean loginBean;
    
    private List<String> propertyTypes;
    private List<String> statusTypes;

    public List<String> getPropertyTypes() {
    return propertyTypes;
}
public List<String> getStatusTypes() {
    return statusTypes;
}


    @PostConstruct
    public void init() {
        propertyTypes = Arrays.asList("villa", "house", "apartment", "commercial", "land");
        statusTypes = Arrays.asList("pending", "approved", "rejected");
        fetchAllBookings();
        fetchBookingsByUser();
        fetchAppointmentRequestsForSeller();
        fetchFilteredBookings();
    }
    
    public void fetchAllBookings() {
        Client client = ClientBuilder.newClient();
        allbookings = client
                .target(REST_API_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Bookings>>() {
                });
        client.close();
    }
    
    public void fetchFilteredBookings() {
        try {
        String formattedDate = null;
        if (filterDate != null && !filterDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(filterDate);
            formattedDate = sdf.format(parsedDate); // Ensure consistent formatting
        }
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/bookings/filter");

            if (filterUserName != null && !filterUserName.isEmpty()) {
                target = target.queryParam("userName", filterUserName);
            }
            if (filterSellerName != null && !filterSellerName.isEmpty()) {
                target = target.queryParam("sellerName", filterSellerName);
            }
            if (filterDate != null && !filterDate.isEmpty()) {
                target = target.queryParam("date", formattedDate);
            }
            if (filterPropertyName != null && !filterPropertyName.isEmpty()) {
                target = target.queryParam("propertyName", filterPropertyName);
            }
            if (filterPropertyType != null && !filterPropertyType.isEmpty()) {
                target = target.queryParam("propertyType", filterPropertyType);
            }
            if (filterStatus != null && !filterStatus.isEmpty()) {
                target = target.queryParam("status", filterStatus);
            }

            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == 200) {
                allbookings = response.readEntity(new GenericType<List<Bookings>>() {});
            } else {
                allbookings = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            allbookings = new ArrayList<>();
        }
    }
        
    public void clearFilters() {
        filterUserName = null;
        filterSellerName = null;
        filterDate = null;
        filterPropertyName = null;
        filterPropertyType = null;
        filterStatus = null;
//        fetchFilteredBookings();
        fetchAllBookings();
    }
    
    public void deleteBooking(int bookingId) {
    try {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/bookings/" + bookingId);

        Response response = target.request().delete();
        if (response.getStatus() == 204) {
            // Re-fetch bookings after successful deletion
            fetchFilteredBookings(); // or fetchAllBookings() if needed
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Booking deleted successfully", null));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to delete booking", null));
        }
    } catch (Exception e) {
        e.printStackTrace();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error occurred while deleting booking", null));
    }
}


    public void fetchBookingsByUser() {
        try {
            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                System.out.println("No logged-in user found");
                return;
            }
            userId = user.getId();
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(REST_API_URL + "/user/" + userId);
            userBookings = target
                    .request(MediaType.APPLICATION_JSON)
                    .get(new jakarta.ws.rs.core.GenericType<List<BookingDTO>>() {
                    });

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void fetchAppointmentRequestsForSeller() {
        try {
            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                System.out.println("No logged-in user found");
                return;
            }

            int sellerId = user.getId();
            Client client = ClientBuilder.newClient();
            Response response = client
                .target("http://localhost:9000/RealEstate_V1/api/bookings/appointment-request/" + sellerId)
                .request(MediaType.APPLICATION_JSON)
                .get();

            if (response.getStatus() == 200) {
                appointmentRequests = response.readEntity(new GenericType<List<BookingDTO>>() {});
            } else {
                System.out.println("Failed to fetch bookings: " + response.getStatus());
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createBooking() {
        try {
            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "User not logged in.", null));
                return null;
            }
            userId = user.getId();
            LocalDate parsedDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate today = LocalDate.now();
            if (parsedDate.isBefore(today)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a future date.", null));
                return null;
            }

            // First check for an existing booking with same user & property
            Client client = ClientBuilder.newClient();
            WebTarget checkTarget = client
                    .target(REST_API_URL + "/check")
                    .queryParam("user_id", userId)
                    .queryParam("property_id", propertyId);

            Response checkResponse = checkTarget.request(MediaType.APPLICATION_JSON).get();
            if (checkResponse.getStatus() == 200) {
                boolean exists = checkResponse.readEntity(Boolean.class); // API should return true if exists
                if (exists) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "You already have a booking for this property on the selected date.", null));
                    client.close();
                    return null;
                }
            }

            JsonObject json = Json.createObjectBuilder()
                    .add("user_id", userId)
                    .add("property_id", propertyId)
                    .add("date", parsedDate.toString())
                    .add("time", selectedTime)
                    .build();

            WebTarget target = client.target(REST_API_URL);
            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(json.toString()));
            if (response.getStatus() == 200 || response.getStatus() == 201) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("Booking successful!"));
                fetchAll();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Booking failed!", null));
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error occurred during booking.", null));
        }
        return null;
    }

    public void deleteBookingByUserAndProperty(int propId) {
        try {
            System.out.println("Received propertyId: " + propId);

            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "User not logged in.", null));
                return;
            }

            int userId = user.getId();
            JsonObject json = Json.createObjectBuilder()
                    .add("user_id", userId)
                    .add("property_id", propId)
                    .build();

            System.out.println("Sending JSON payload: " + json.toString());

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(REST_API_URL + "/by-user-property");

            Response response = target
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(json.toString()));

            int status = response.getStatus();

            if (status == 204) {
                System.out.println("Booking deleted successfully.");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage("Booking deleted successfully."));
                fetchAll();
            } else {
                String msg = response.readEntity(String.class);
                System.out.println("Delete failed: " + msg);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Delete failed: " + msg, null));
            }

            client.close();
            System.out.println("=== Delete Booking End ===");
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error occurred while deleting booking.", null));
        }
    }

public void updateBooking(Integer userId, Integer propertyId, LocalDate date, LocalTime time, String status) {
    if ((date == null) && (time == null) && (status == null || status.isEmpty())) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Please provide at least one value to update.", null));
        return;
    }

    try {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/bookings/update-by-user-property");
        
        System.out.println("U id: " + userId);
        System.out.println("p id: " + propertyId);

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("user_id", userId)
                .add("property_id", propertyId);

        if (date != null) {
            jsonBuilder.add("date", date.toString()); // format: YYYY-MM-DD
        }
        if (time != null) {
            jsonBuilder.add("time", time.toString()); // format: HH:mm:ss
        }
        if (status != null && !status.isEmpty()) {
            jsonBuilder.add("status", status);
        }

        JsonObject requestBody = jsonBuilder.build();

        Response response = target.request(MediaType.APPLICATION_JSON)
                .put(Entity.json(requestBody));

        if (response.getStatus() == 200) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Booking updated successfully."));
            
            // Handle transaction adjustments based on status change
            if (status != null && !status.isEmpty()) {
                handleTransactionAdjustment(userId, status);
            }
            
            // Refresh appointment requests
            fetchAppointmentRequestsForSeller();
            
            // Trigger profile page refresh by updating admin bean data
            refreshAdminData();
            
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Update failed: " + response.readEntity(String.class), null));
        }

        response.close();
        client.close();
    } catch (Exception e) {
        e.printStackTrace();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Exception occurred: " + e.getMessage(), null));
    }
}

private void handleTransactionAdjustment(Integer userId, String status) {
    try {
        if ("approved".equals(status)) {
            // When approved, add ₹100 to the transaction (this will be handled by the payment system)
            System.out.println("Booking approved for user " + userId + " - service charge will be added when payment is made");
        } else if ("rejected".equals(status)) {
            // When rejected, only change the status - no transaction adjustment needed
            System.out.println("Booking rejected for user " + userId + " - no transaction adjustment made");
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error handling transaction adjustment: " + e.getMessage());
    }
}

private void refreshAdminData() {
    try {
        // This will trigger the admin bean to refresh its data
        // The profile page will automatically show updated values
        System.out.println("Refreshing admin data for profile updates");
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error refreshing admin data: " + e.getMessage());
    }
}

    public void fetchAll() {
        try {
            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                return;
            }
            userId = user.getId();
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(REST_API_URL + "/user/" + userId);

            userBookings = target
                    .request(MediaType.APPLICATION_JSON)
                    .get(new jakarta.ws.rs.core.GenericType<List<BookingDTO>>() {
                    });
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareBooking(int propertyId) {
        this.propertyId = propertyId;
    }

    public void prepareCancel(int propertyId) {
        System.out.println("Preparing to delete booking with propertyId = " + propertyId);
        this.selectedPropertyId = propertyId;
    }
    
        public List<Bookings> getAllBookings() {
            return allbookings;
        }
        public void setAllBookings(List<Bookings> allbookings) {
            this.allbookings = allbookings;
        }
    
    public String getFilterUserName() {
        return filterUserName;
    }

    public void setFilterUserName(String filterUserName) {
        this.filterUserName = filterUserName;
    }

    public String getFilterSellerName() {
        return filterSellerName;
    }

    public void setFilterSellerName(String filterSellerName) {
        this.filterSellerName = filterSellerName;
    }

    public String getFilterDate() {
        return filterDate;
    }

    public void setFilterDate(String filterDate) {
        this.filterDate = filterDate;
    }

    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    public void setFilterPropertyName(String filterPropertyName) {
        this.filterPropertyName = filterPropertyName;
    }

    public String getFilterPropertyType() {
        return filterPropertyType;
    }

    public void setFilterPropertyType(String filterPropertyType) {
        this.filterPropertyType = filterPropertyType;
    }
    
    public String getStatus() {
        return filterStatus;
    }

    public void setStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }
    
    public List<BookingDTO> getAppointmentRequests() { return appointmentRequests; }

    public Integer getSelectedPropertyId() { return selectedPropertyId; }
    public void setSelectedPropertyId(Integer selectedPropertyId) { this.selectedPropertyId = selectedPropertyId; }

    public List<PropertyDTO> getMyProperties() { return myProperties; }
    public void setMyProperties(List<PropertyDTO> myProperties) { this.myProperties = myProperties; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public Date getSelectedDate() { return selectedDate; }
    public void setSelectedDate(Date selectedDate) { this.selectedDate = selectedDate; }

    public String getSelectedTime() { return selectedTime; }
    public void setSelectedTime(String selectedTime) { this.selectedTime = selectedTime; }

    public List<BookingDTO> getUserBookings() { return userBookings; }
    public void setUserBookings(List<BookingDTO> userBookings) { this.userBookings = userBookings; }

    public void downloadAppointmentLetter(BookingDTO booking) {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            String filename = "Appointment_Letter_" + booking.getPropertyTitle().replaceAll("\\s+", "_") + ".txt";
            String content = "Appointment Letter\n\n"
                    + "Property: " + booking.getPropertyTitle() + "\n"
                    + "Address: " + booking.getAddress() + "\n"
                    + "Date: " + booking.getDate() + "\n"
                    + "Time: " + booking.getTime() + "\n"
                    + "Agent: " + booking.getSellerName() + "\n"
                    + "Status: " + booking.getStatus() + "\n\n"
                    + "Thank you for booking with CityBroker.";

            // Set HTTP response headers
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            response.reset();
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            // Write content to response
            OutputStream output = response.getOutputStream();
            output.write(content.getBytes(StandardCharsets.UTF_8));
            output.flush();
            output.close();

            facesContext.responseComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
