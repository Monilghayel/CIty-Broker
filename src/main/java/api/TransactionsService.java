package api;

import entity.Transactions;
import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Stateless
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionsService {

    @PersistenceContext(unitName = "RealEstatePU")
    private EntityManager em;

    @GET
    public List<Transactions> getAllTransactions() {
        return em.createNamedQuery("Transactions.findAll", Transactions.class).getResultList();
    }

    @GET
    @Path("{id}")
    public Transactions getTransactionById(@PathParam("id") Integer id) {
        return em.find(Transactions.class, id);
    }

    @GET
    @Path("user/{userId}")
    public List<Transactions> getTransactionsByUserId(@PathParam("userId") Integer userId) {
        return em.createQuery("SELECT t FROM Transactions t WHERE t.userId.id = :userId", Transactions.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
    
    @POST
public Response createOrUpdateTransaction(JsonObject json) {
    try {
        Integer userId = json.getInt("user_id");
        BigDecimal amount = new BigDecimal(json.getJsonNumber("amount").toString());

        // Step 1: Validate user
        Users user = em.find(Users.class, userId);
        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("User not found with ID: " + userId)
                           .build();
        }

        // Step 2: Check if a transaction already exists for this user
        List<Transactions> existingList = em.createQuery(
            "SELECT t FROM Transactions t WHERE t.userId.id = :userId", Transactions.class)
            .setParameter("userId", userId)
            .getResultList();

        Transactions transaction;
        if (!existingList.isEmpty()) {
            // Update existing transaction
            transaction = existingList.get(0);
            transaction.setAmount(transaction.getAmount().add(amount));
            transaction.setCreatedAt(new Date());
        } else {
            // Create new transaction
            transaction = new Transactions();
            transaction.setUserId(user);
            transaction.setAmount(amount);
            transaction.setCreatedAt(new Date());
            em.persist(transaction);
        }

        return Response.status(Response.Status.OK).entity(transaction).build();

    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("Server error occurred.")
                       .build();
    }
}


//    @POST
//    public Response createTransaction(Transactions transaction) {
//        transaction.setCreatedAt(new Date()); // set current time
//        em.persist(transaction);
//        return Response.status(Response.Status.CREATED).entity(transaction).build();
//    }

    @DELETE
    @Path("{id}")
    public Response deleteTransaction(@PathParam("id") Integer id) {
        Transactions tx = em.find(Transactions.class, id);
        if (tx == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.remove(tx);
        return Response.ok().build();
    }
}
