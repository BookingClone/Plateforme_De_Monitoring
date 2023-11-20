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
import java.util.List;

@Named("filebean")
@SessionScoped
public class Filebean implements Serializable {


    FileUploadEJB fileUploadEJB = new FileUploadEJB();

    FacesContext facesContext = FacesContext.getCurrentInstance();
    String userId = (String) facesContext.getExternalContext().getSessionMap().get("userId");

    List<Document> hebergements=fileUploadEJB.getLogs(userId);
    int CountLogs = fileUploadEJB.getCountLogs(userId);
    int CountDocuments = fileUploadEJB.getCountDocs(userId);

    public List<Document> getHebergements() {
        hebergements=fileUploadEJB.getLogs(userId);
        return hebergements;
    }

    public void setHebergements(List<Document> hebergements) {
        this.hebergements = hebergements;
    }

    public int getCountLogs() {
        CountLogs = fileUploadEJB.getCountLogs(userId);
        return CountLogs;
    }

    public void setCountLogs(int countLogs) {
        CountLogs = countLogs;
    }

    public int getCountDocuments() {
        CountDocuments = fileUploadEJB.getCountDocs(userId);
        return CountDocuments;
    }

    public void setCountDocuments(int countDocuments) {
        CountDocuments = countDocuments;
    }
}
