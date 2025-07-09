package ejb;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Named("paymentSuccessBean")
@RequestScoped
public class PaymentSuccessBean implements Serializable {

    private static final String TRANSACTIONS_API_URL = "http://localhost:9000/RealEstate_V1/api/transactions";

    private Integer userId;
    private BigDecimal amount;
    private boolean transactionRecorded = false;
    private boolean alreadyProcessed = false;

    @PostConstruct
    public void init() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();

            String userIdStr = params.get("userId");
            String amountStr = params.get("amount");

            if (userIdStr != null && amountStr != null) {
                this.userId = Integer.parseInt(userIdStr);
                this.amount = new BigDecimal(amountStr);

                // Check if this payment has already been processed
                if (!isPaymentAlreadyProcessed()) {
                    // Record the transaction
                    recordTransaction();
                    // Mark this payment as processed
                    markPaymentAsProcessed();
                } else {
                    this.transactionRecorded = true; // Show as recorded since it was already processed
                    this.alreadyProcessed = true;
                    System.out.println("Payment already processed for user " + userId + " with amount " + amount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error processing payment success: " + e.getMessage(), null));
        }
    }

    private boolean isPaymentAlreadyProcessed() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

            if (session != null) {
                String paymentKey = "payment_" + userId + "_" + amount;
                return session.getAttribute(paymentKey) != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markPaymentAsProcessed() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

            if (session != null) {
                String paymentKey = "payment_" + userId + "_" + amount;
                session.setAttribute(paymentKey, true);
                // Set expiration time (e.g., 1 hour)
                session.setMaxInactiveInterval(3600);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordTransaction() {
        try {
            JsonObject json = Json.createObjectBuilder()
                    .add("user_id", userId)
                    .add("amount", amount)
                    .build();

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(TRANSACTIONS_API_URL);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(json.toString()));

            if (response.getStatus() == 200) {
                this.transactionRecorded = true;
                System.out.println("Transaction recorded successfully for user ID: " + userId + " with amount: " + amount);
            } else {
                String errorMsg = response.readEntity(String.class);
                System.err.println("Failed to record transaction: " + errorMsg);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Payment successful but transaction recording failed: " + errorMsg, null));
            }

            response.close();
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error recording transaction: " + e.getMessage(), null));
        }
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isTransactionRecorded() {
        return transactionRecorded;
    }

    public void setTransactionRecorded(boolean transactionRecorded) {
        this.transactionRecorded = transactionRecorded;
    }

    public boolean isAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setAlreadyProcessed(boolean alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }
}
