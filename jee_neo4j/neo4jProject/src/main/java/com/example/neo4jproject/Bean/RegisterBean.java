package com.example.neo4jproject.Bean;

import com.example.neo4jproject.DB.MongoDBManager;
import com.example.neo4jproject.DB.Neo4jConnexion;
import com.example.neo4jproject.entities.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;

import java.io.Serializable;
import java.util.logging.Logger;

@Named("registerBean")
@SessionScoped
public class RegisterBean implements Serializable{
    private MongoDBManager mongoDBUtil = new MongoDBManager();
    private Neo4jConnexion neo4jConnexion = new Neo4jConnexion();
    private Driver driver;
    private User user = new User();

    private static final Logger LOGGER = Logger.getLogger(RegisterBean.class.getName());

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String register() {
        try {
            driver = neo4jConnexion.getDriver();
            Session session = driver.session();
            String query = "CREATE (u:User) SET u.firstname = $firstname, " +
                    "u.lastname = $lastname, u.email = $email, u.password = $password RETURN u.email + ', from node ' + id(u)";
            Result result = session.run(query, parameters(
                    "firstname", user.getFirstName(),
                    "lastname", user.getLastName(),
                    "email", user.getEmail(),
                    "password", user.getPassword()));


            // Store the user's ID in the session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (httpSession != null) {
                httpSession.setAttribute("userEmail", user.getEmail());
            }

            LOGGER.info("User registered successfully: " + user.getEmail());
            System.out.println("ohhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Error during user registration: " + e.getMessage());
            return "error.xhtml"; // Handle registration errors
        }
        System.out.println("dddddddddddddddddddddddddddddddddddddd");
        return "login.xhtml"; // Redirect to a success page
    }

}
