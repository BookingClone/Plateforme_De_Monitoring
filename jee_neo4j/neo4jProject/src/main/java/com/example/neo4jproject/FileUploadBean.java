package com.example.neo4jproject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Named
@RequestScoped
public class FileUploadBean {
    @EJB
    private FileUploadEJB fileUploadEJB;

    private List<Document> distinctValues;

    public List<Document> getDistinctValues() {
        return distinctValues;
    }
    private Part uploadedFile;

    public FileUploadEJB getFileUploadEJB() {
        return fileUploadEJB;
    }

    public void setFileUploadEJB(FileUploadEJB fileUploadEJB) {
        this.fileUploadEJB = fileUploadEJB;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    // Getter and setter for uploadedFile

    public String uploadFile() {
        // Obtenir l'ID de l'utilisateur depuis la session
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String userId = (String) facesContext.getExternalContext().getSessionMap().get("userId");
        String userEmail = (String) facesContext.getExternalContext().getSessionMap().get("userEmail");
        if (userId != null) {
            fileUploadEJB.uploadFile(uploadedFile, userId, userEmail); // Passer le userId
            return "visualiser.xhtml"; // Redirect to a confirmation page
        } else {
            // Gérer l'erreur, par exemple, renvoyer à une page d'erreur
            return "error";
        }
    }
}

