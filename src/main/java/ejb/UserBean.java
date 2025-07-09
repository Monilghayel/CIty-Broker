package ejb;

import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class UserBean implements Serializable {

    private List<Users> users;
    private Users selectedUser = new Users();

    @PostConstruct
    public void init() {
        try {
        String token = (String) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("token");

        if (token == null || token.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext()
                .redirect("login.xhtml");
        } else {
            fetchLoggedInUser();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    public void fetchLoggedInUser() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequest();

            String token = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null && !token.isEmpty()) {
                Client client = ClientBuilder.newClient();
                WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/users/me");

                Response response = target.request(MediaType.APPLICATION_JSON)
                        .cookie(new jakarta.ws.rs.core.Cookie("token", token)) // JAX-RS Cookie
                        .get();

                if (response.getStatus() == 200) {
                    selectedUser = response.readEntity(Users.class);
                } else {
                    System.err.println("Failed to fetch user: " + response.readEntity(String.class));
                }
                client.close();
            } else {
                System.err.println("No token cookie found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchUsers() {
        Client client = ClientBuilder.newClient();
        users = client
            .target("http://localhost:9000/RealEstate_V1/api/users")
            .request(MediaType.APPLICATION_JSON)
            .get(new GenericType<List<Users>>() {});
        client.close();
    }

    public void createUser() {
        Client client = ClientBuilder.newClient();
        client
            .target("http://localhost:9000/RealEstate_V1/api/users")
            .request()
            .post(Entity.entity(selectedUser, MediaType.APPLICATION_JSON));
        client.close();
        fetchUsers();
        selectedUser = new Users();
    }

    public void updateUser() {
        try {
            String token = (String) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .get("token");

            if (token != null && selectedUser.getId() != null) {
                Client client = ClientBuilder.newClient();
                client
                        .target("http://localhost:9000/RealEstate_V1/api/users/" + selectedUser.getId())
                        .request()
                        .put(Entity.entity(selectedUser, MediaType.APPLICATION_JSON));
                client.close();
                fetchLoggedInUser();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(int id) {
        Client client = ClientBuilder.newClient();
        client
            .target("http://localhost:9000/RealEstate_V1/api/users/" + id)
            .request()
            .delete();
        client.close();
        fetchUsers();
    }

    public List<Users> getUsers() { return users; }
    public Users getSelectedUser() { return selectedUser; }
    public void setSelectedUser(Users selectedUser) { this.selectedUser = selectedUser; }
}
