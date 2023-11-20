package com.example.demo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;

import java.io.Serializable;
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {


    private User user = new User();
    private boolean loggedIn;



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String login() {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost", 27017); // Adresse et port de votre serveur MongoDB
            MongoDatabase database = mongoClient.getDatabase("monitoring"); // Remplacez par le nom de votre base de données
            MongoCollection<Document> usersCollection = database.getCollection("users"); // Remplacez par le nom de votre collection d'utilisateurs

            Document query = new Document("email", getUser().getEmail()).append("password", getUser().getPassword());
            Document userDocument = usersCollection.find(query).first();
            if (usersCollection.countDocuments(query) == 1) {
                user = new User();
                user.setEmail(userDocument.getString("email"));
                user.setFirstName(userDocument.getString("firstName"));
                user.setLastName(userDocument.getString("lastName"));
                loggedIn = true;
                // Store the user's ID in the session
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
                if (session != null) {
                    session.setAttribute("userId", userDocument.getObjectId("_id").toString());
                }

                return "dashboard.xhtml";
            } else {
                loggedIn = false;
                return "login";
            }
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    }
}
