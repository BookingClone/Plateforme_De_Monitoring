package com.example.neo4jproject.Bean;

import com.example.neo4jproject.DB.Neo4jConnexion;
import com.example.neo4jproject.entities.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import java.io.Serializable;
import static org.neo4j.driver.Values.parameters;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {
    private Neo4jConnexion neo4jConnection = new Neo4jConnexion();
    private Driver driver;
    private User user = new User();
    private boolean loggedIn;



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String login() {
        try {
            // Neo4j Insertion
            driver = neo4jConnection.getDriver();
            Session session = driver.session();
            String query = "MATCH (u:User {email: $email, password: $password}) RETURN id(u) as userId, u.email as emailUser, u.firstname as fName,u.lastname as lName";
            Result result = session.run(query, parameters("email", user.getEmail(), "password", user.getPassword()));

            // Vérifier si le nœud existe
            if (result.hasNext()) {
                // Le nœud existe, vous pouvez récupérer ses données
                Record record = result.next();
                user.setId(String.valueOf(record.get("userId")));
                user.setFirstName(record.get("fName").asString());
                user.setLastName(record.get("lName").asString());
                user.setEmail(record.get("emailUser").asString());
                System.out.println("email "+user.getEmail());
                loggedIn = true;
                // Store the user's ID in the session
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession sessionhttp = (HttpSession) facesContext.getExternalContext().getSession(false);
                if (sessionhttp != null) {
                    sessionhttp.setAttribute("userId", user.getId().toString());
                    sessionhttp.setAttribute("userEmail", user.getEmail());
                }
                return "dashboard.xhtml";
            } else {
                // Le nœud n'existe pas
                System.out.println("Aucun nœud correspondant n'a été trouvé.");
                return "login.xhtml";
            }

        } finally {

            if (neo4jConnection != null) {
                neo4jConnection.close();
            }

        }

    }
}