package ejb;

import entity.Users;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.Date;
import utils.ToastUtil;

@Named
@RequestScoped
public class RegisterBean implements Serializable {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private boolean termsAccepted;
    private String role = "buyer";
    @Inject
    private ToastUtil toastUtil;

public void register() {
    if (password == null || password.length() < 8) {
        toastUtil.showError("Password must be at least 8 characters long.");
        return;
    }

    if (!password.equals(confirmPassword)) {
        toastUtil.showError("Passwords do not match.");
        return;
    }

    try {
        Users newUser = new Users();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setCreatedAt(new Date());

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/users");

        Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(newUser, MediaType.APPLICATION_JSON));

        if (response.getStatus() == 201) {
            toastUtil.showSuccess("Account created successfully. You may now log in.");
        } else {
            toastUtil.showError("Failed to register. Try again.");
        }

    } catch (Exception e) {
        e.printStackTrace();
        toastUtil.showError("Registration error: " + e.getMessage());
    }
}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }
}
