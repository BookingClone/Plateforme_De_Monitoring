package com.example.neo4jproject;

import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import org.bson.Document;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class MonitoringBean implements Serializable {
    @EJB
    private FileUploadEJB fileUploadEJB;

    List<Document> hebergements;

    public List<Document> getHebergements() {
        return hebergements;
    }

    public void setHebergements(List<Document> hebergements) {
        this.hebergements = hebergements;
    }

    public FileUploadEJB getFileUploadEJB() {
        return fileUploadEJB;
    }

    public void setFileUploadEJB(FileUploadEJB fileUploadEJB) {
        this.fileUploadEJB = fileUploadEJB;
    }

    private String selectedLogfile;

    public String getSelectedLogfile() {
        return selectedLogfile;
    }

    public void setSelectedLogfile(String selectedLogfile) {
        this.selectedLogfile = selectedLogfile;
    }

    public String navigateToMonitoring() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String userId = (String) facesContext.getExternalContext().getSessionMap().get("userId");
        if (userId != null) {
            hebergements=fileUploadEJB.getVisDocs(selectedLogfile,userId);
            return "monitoring.xhtml?faces-redirect=true";
        } else {
            // Gérer l'erreur, par exemple, renvoyer à une page d'erreur
            return "error";
        }
    }
}
