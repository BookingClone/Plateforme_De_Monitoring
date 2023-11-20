package com.example.neo4jproject;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadUtil {

    public static String uploadFile(Part uploadedFile, String uploadDirectory) {
        String fileName = Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString();

        try {
            String realPath = "/resources/uploads/"; // Spécifiez le chemin réel de votre dossier "uploads"

            // Créez le répertoire "uploads" si nécessaire
            Files.createDirectories(Paths.get(realPath));

            // Utilisez la méthode Files.copy pour enregistrer le fichier dans le dossier "uploads"
            Path fileDestinationPath = Paths.get(realPath, fileName);
            Files.copy(uploadedFile.getInputStream(), fileDestinationPath);

            // Renvoyez le chemin du fichier enregistré
            return fileDestinationPath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // En cas d'erreur
        }
    }
}
