package api;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/payments")
public class PaymentService {

    @POST
    @Path("/create-session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCheckoutSession(@FormParam("amount") long amount, 
                                        @FormParam("userId") int userId,
                                        @FormParam("customerEmail") String customerEmail) {
        try {
            // Validate input parameters
            if (amount <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Error: Amount must be greater than 0")
                        .build();
            }
            
            if (userId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Error: Invalid user ID")
                        .build();
            }
            
            // Use provided email or fallback
            String email = (customerEmail != null && !customerEmail.trim().isEmpty()) 
                          ? customerEmail.trim() 
                          : "customer@example.com";
            
            System.out.println("Creating payment session for user ID: " + userId + " with amount: " + amount + " paise for email: " + email);
            
            Stripe.apiKey = "sk_test_51PKoPUSGZD8cOeW2ThOg2Dn1iOf0iXBV2m3kF2PED8tS0ZUoNw4lGEhBgsFlVVUn7L1L5dTx7FvyZvalx6O74fKZ00xSLncvwb";

            SessionCreateParams params =
                SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:9000/RealEstate_V1/payment-success.xhtml?userId=" + userId + "&amount=" + (amount/100))
                    .setCancelUrl("http://localhost:9000/RealEstate_V1/payment-cancelled.xhtml")
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setCustomerEmail(email)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("inr")
                                    .setUnitAmount(amount) // in paise (e.g., 1000 = ₹10)
                                    .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Service Charges Payment")
                                            .setDescription("Payment for service charges - User ID: " + userId)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();

            System.out.println("Stripe session parameters created. Attempting to create session...");
            
            Session session;
            try {
                session = Session.create(params);
            } catch (Exception e) {
                System.out.println("First attempt failed, trying without explicit payment method types...");
                // Try without explicit payment method types
                SessionCreateParams fallbackParams =
                    SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:9000/RealEstate_V1/payment-success.xhtml?userId=" + userId + "&amount=" + (amount/100))
                        .setCancelUrl("http://localhost:9000/RealEstate_V1/payment-cancelled.xhtml")
                        .setCustomerEmail(email)
                        .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                        .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("inr")
                                        .setUnitAmount(amount)
                                        .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName("Service Charges Payment")
                                                .setDescription("Payment for service charges - User ID: " + userId)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build();
                try {
                    session = Session.create(fallbackParams);
                } catch (Exception e2) {
                    System.out.println("Second attempt failed, trying with USD currency...");
                    // Try with USD currency as a test
                    SessionCreateParams usdParams =
                        SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:9000/RealEstate_V1/payment-success.xhtml?userId=" + userId + "&amount=" + (amount/100))
                            .setCancelUrl("http://localhost:9000/RealEstate_V1/payment-cancelled.xhtml")
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            .setCustomerEmail(email)
                            .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                            .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount(amount / 100) // Convert to USD cents (rough conversion)
                                            .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName("Service Charges Payment")
                                                    .setDescription("Payment for service charges - User ID: " + userId)
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build();
                    session = Session.create(usdParams);
                }
            }
            
            System.out.println("Payment session created successfully. Session ID: " + session.getId());

            JsonObject json = Json.createObjectBuilder()
                    .add("checkoutUrl", session.getUrl())
                    .add("sessionId", session.getId())
                    .build();

            return Response.ok(json).build();

        } catch (Exception e) {
            System.err.println("Error creating payment session: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }
}
