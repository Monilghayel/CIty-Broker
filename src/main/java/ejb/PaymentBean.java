/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ejb;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;

@Named("paymentBean")
@RequestScoped
public class PaymentBean implements Serializable {

    @Inject
    private LoginBean loginBean;

    public String redirectToPayment() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, String> params = ec.getRequestParameterMap();

            // Get and validate parameters
            String userIdStr = params.get("userId");
            String amountStr = params.get("amount");
            
            if (userIdStr == null || amountStr == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Missing user ID or amount", null));
                return null;
            }

            int userId = Integer.parseInt(userIdStr);
            long amountInRupees = Long.parseLong(amountStr);
            
            if (amountInRupees <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid amount", null));
                return null;
            }
            
            long amountInPaise = amountInRupees * 100; // Stripe expects smallest currency unit

            // Get user email for compliance
            String userEmail = "customer@example.com"; // Default fallback
            if (loginBean != null && loginBean.getLoggedInUser() != null) {
                userEmail = loginBean.getLoggedInUser().getEmail();
            }

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/payments/create-session");

            Form form = new Form();
            form.param("amount", String.valueOf(amountInPaise)); // Stripe expects paise (if INR)
            form.param("userId", String.valueOf(userId));
            form.param("customerEmail", userEmail);

            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form));

            if (response.getStatus() == 200) {
                JsonObject json = response.readEntity(JsonObject.class);
                String checkoutUrl = json.getString("checkoutUrl");
                
                if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                    ec.redirect(checkoutUrl);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid checkout URL received", null));
                }
            } else {
                // Read error response for better debugging
                String errorResponse = response.readEntity(String.class);
                System.err.println("Payment API Error - Status: " + response.getStatus() + ", Response: " + errorResponse);
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment session creation failed. Status: " + response.getStatus(), null));
            }

            response.close();
            client.close();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid user ID or amount format", null));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Exception: " + e.getMessage(), null));
        }

        return null;
    }
}

