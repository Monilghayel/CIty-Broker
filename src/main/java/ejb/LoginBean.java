package ejb;

import dto.UserLoginRequest;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import utils.ToastUtil;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    private String email;
    private String password;
    private boolean rememberMe;
    private Users loggedInUser;
    @Inject
    private ToastUtil toastUtil;

    @PostConstruct
    public void init() {
        if (tokenInSession()) {
            System.out.println("[LoginBean] Token found in session.");
            return;
        }

        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = context.getSessionMap();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    sessionMap.put("token", token);
                    System.out.println("[LoginBean] Token restored from cookie.");

                    try {
                        Client client = ClientBuilder.newClient();
                        Response userResponse = client
                                .target("http://localhost:9000/RealEstate_V1/api/users/me")
                                .request(MediaType.APPLICATION_JSON)
                                .cookie("token", token)
                                .get();

                        if (userResponse.getStatus() == 200) {
                            loggedInUser = userResponse.readEntity(Users.class);
                            System.out.println("[LoginBean] Logged in user restored from token: " + loggedInUser.getEmail());
                        } else {
                            System.out.println("[LoginBean] Failed to fetch user with token. Status: " + userResponse.getStatus());
                        }
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public void login() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:9000/RealEstate_V1/api/users/login");

            UserLoginRequest request = new UserLoginRequest();
            request.setEmail(email);
            request.setPassword(password);

            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200) {
                JsonObject json = response.readEntity(JsonObject.class);
                String token = json.getString("token");

                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("token", token);

                // Set token cookie
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                HttpServletResponse httpResponse = (HttpServletResponse) externalContext.getResponse();

                Cookie cookie = new Cookie("token", token);
                cookie.setPath("/");
                cookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
                httpResponse.addCookie(cookie);

                // Fetch logged-in user info
                Response userResponse = client
                        .target("http://localhost:9000/RealEstate_V1/api/users/me")
                        .request(MediaType.APPLICATION_JSON)
                        .cookie("token", token)
                        .get();

                if (userResponse.getStatus() == 200) {
                    loggedInUser = userResponse.readEntity(Users.class);
                    System.out.println("[LoginBean] Login successful. Logged in user: " + loggedInUser.getEmail());
                } else {
                    System.out.println("[LoginBean] Failed to retrieve user after login.");
                }

                client.close();

                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("login-callback.xhtml?token=" + token);

            } else {
                toastUtil.showError("Invalid email or password.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Redirect error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Login error: " + e.getMessage()));
        }
    }

    public String logout() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.getSessionMap().remove("token");

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        Cookie cookie = new Cookie("token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        loggedInUser = null;

        externalContext.invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    private boolean tokenInSession() {
        return FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("token") != null;
    }

    public boolean isLoggedIn() {
        return tokenInSession() && loggedInUser != null;
    }

    // Getters and Setters
    public Users getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(Users loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
