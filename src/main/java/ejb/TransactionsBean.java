package ejb;

import entity.Transactions;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named("transactionsBean")
@RequestScoped
public class TransactionsBean implements Serializable {

    private static final String REST_API_URL = "http://localhost:9000/RealEstate_V1/api/transactions";

    private BigDecimal amount;
    private List<Transactions> alltransactions;

    @Inject
    private LoginBean loginBean;

    @PostConstruct
    public void init() {
        allTransactions();

    }

    public void allTransactions() {
        Client client = ClientBuilder.newClient();
        alltransactions = client
                .target(REST_API_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Transactions>>() {
                });
        client.close();
    }

    public void submitTransaction() {
        try {
            Users user = loginBean.getLoggedInUser();
            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "User not logged in.", null));
                return;
            }

            int userId = user.getId();

            JsonObject json = Json.createObjectBuilder()
                    .add("user_id", userId)
                    .add("amount", amount)
                    .build();

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(REST_API_URL);
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(json.toString()));

            if (response.getStatus() == 200) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction successful.", null));
            } else {
                String errorMsg = response.readEntity(String.class);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Transaction failed: " + errorMsg, null));
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occurred while processing the transaction.", null));
        }
    }

    public List<Transactions> getAllTransactions() {
        return alltransactions;
    }

    public void setAllTransactions(List<Transactions> alltransactions) {
        this.alltransactions = alltransactions;
    }

    // Getter and Setter
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
