package com.example.neo4jproject;

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
    String userEmail = (String) facesContext.getExternalContext().getSessionMap().get("userEmail");


    List<Document> hebergements=fileUploadEJB.getLogs(userEmail);
    long CountLogs = fileUploadEJB.getCountLogs(userEmail);
    long CountDocuments = fileUploadEJB.getCountDocs(userEmail);

    public List<Document> getHebergements() {
        hebergements=fileUploadEJB.getLogs(userEmail);
        CountLogs = fileUploadEJB.getCountLogs(userEmail);
        CountDocuments = fileUploadEJB.getCountDocs(userEmail);

        return hebergements;
    }

    public void setHebergements(List<Document> hebergements) {
        hebergements=fileUploadEJB.getLogs(userEmail);
        CountLogs = fileUploadEJB.getCountLogs(userEmail);
        CountDocuments = fileUploadEJB.getCountDocs(userEmail);
        this.hebergements = hebergements;
    }

    public long getCountLogs() {
        return CountLogs;
    }

    public void setCountLogs(int countLogs) {
        CountLogs = countLogs;
    }

    public long getCountDocuments() {
        return CountDocuments;
    }

    public void setCountDocuments(int countDocuments) {
        CountDocuments = countDocuments;
    }

    public void deleteFile(String fileName){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String userEmail = (String) facesContext.getExternalContext().getSessionMap().get("userEmail");
        fileUploadEJB.deletedFile(fileName,userEmail);
        hebergements=fileUploadEJB.getLogs(userEmail);
        CountLogs = fileUploadEJB.getCountLogs(userEmail);
        CountDocuments = fileUploadEJB.getCountDocs(userEmail);
    }
}
