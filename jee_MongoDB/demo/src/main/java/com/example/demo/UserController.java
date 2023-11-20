package com.example.demo;

import com.mongodb.client.MongoCollection;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;

import java.io.Serializable;
import java.util.logging.Logger;

@Named("userController")
@SessionScoped
public class UserController implements Serializable {

    private MongoDBManager mongoDBUtil = new MongoDBManager();

    private User user = new User();

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String register() {
        try {
            MongoCollection<Document> collection = mongoDBUtil.getCollection("monitoring", "users");

            Document newUser = new Document()
                    .append("firstName", user.getFirstName())
                    .append("lastName", user.getLastName())
                    .append("email", user.getEmail())
                    .append("password", user.getPassword());

            collection.insertOne(newUser);

            // Store the user's ID in the session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                session.setAttribute("userId", newUser.getObjectId("_id").toString());
            }

            LOGGER.info("User registered successfully: " + user.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Error during user registration: " + e.getMessage());
            return "error.xhtml"; // Handle registration errors
        }

        return "login.xhtml"; // Redirect to a success page
    }

}
