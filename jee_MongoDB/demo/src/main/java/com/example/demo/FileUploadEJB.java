package com.example.demo;

import com.mongodb.client.*;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.apache.commons.lang3.tuple.Pair; // You can use a similar class or create your own Pair class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Named
public class FileUploadEJB {
    public void uploadFile(Part uploadedFile, String userId) {
        if (uploadedFile != null) {
            // Obtain the name of the uploaded file
            String fileName = getFileName(uploadedFile);

            try {
                // Access the collection using MongoDBManager
                MongoClient mongoClient = MongoDBManager.getMongoClient();
                MongoDatabase database = mongoClient.getDatabase("monitoring");
                MongoCollection<Document> collection = database.getCollection("monitoring");

                // Access the userLog collection
                MongoCollection<Document> userLogCollection = database.getCollection("userLog");

                // Read the file and insert into MongoDB
                try (BufferedReader br = new BufferedReader(new InputStreamReader(uploadedFile.getInputStream()))) {
                    String line;
                    int insertedCount = 0;
                    int failedCount = 0;

                    while ((line = br.readLine()) != null) {
                        Document logDocument = parseLogLine(line);
                        if (logDocument != null) {
                            // Insert the log document into "monitoring" collection
                            collection.insertOne(logDocument);
                            insertedCount++;

                            // Create a userLog document with user ID and monitoring document ID
                            Document userLogDocument = new Document()
                                    .append("userId", userId)
                                    .append("monitoringDocumentId", logDocument.getObjectId("_id"))
                                    .append("logfile", fileName);

                            // Insert the userLog document into "userLog" collection
                            userLogCollection.insertOne(userLogDocument);
                        } else {
                            failedCount++;
                        }
                    }
                    System.out.println("Import completed. Inserted lines: " + insertedCount + ", Failed lines: " + failedCount);
                }

                System.out.println("File uploaded successfully: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        Pattern pattern = Pattern.compile("filename=\"(.+?)\"");
        Matcher matcher = pattern.matcher(contentDisposition);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    private static Document parseLogLine(String line) {
        Document logDocument = new Document();

        Pair<Integer, Matcher> patternAndMatcher = findPattern(line);

        if (patternAndMatcher != null) {
            int patternIndex = patternAndMatcher.getLeft();
            Matcher matcher = patternAndMatcher.getRight();

            switch (patternIndex) {
                case 0:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("ipAddress", matcher.group(2))
                            .append("method", matcher.group(3))
                            .append("request", matcher.group(4))
                            .append("statusCode", matcher.group(5))
                            .append("response", matcher.group(6))
                            .append("duration", matcher.group(7));
                    break;
                case 1:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("username", matcher.group(2))
                            .append("password", matcher.group(3))
                            .append("authentificationStatus", matcher.group(4));
                    break;
                case 2:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("username", matcher.group(2))
                            .append("password", matcher.group(3))
                            .append("authentificationStatus", matcher.group(4))
                            .append("ipAddress", matcher.group(5));
                    break;
                case 3:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("sessionId", matcher.group(2))
                            .append("userId", matcher.group(3))
                            .append("action", matcher.group(4))
                            .append("duration", matcher.group(5))
                            .append("status", matcher.group(6));
                    break;
                case 4:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("sessionId", matcher.group(2))
                            .append("userId", matcher.group(3))
                            .append("action", matcher.group(4))
                            .append("duration", matcher.group(5));
                    break;
                case 5:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("sessionId", matcher.group(2))
                            .append("userId", matcher.group(3))
                            .append("action", matcher.group(4))
                            .append("duration", matcher.group(5))
                            .append("path", matcher.group(6));

                    if (matcher.group(7) != null && matcher.group(7).equals("error")) {
                        logDocument.append("status", "error");
                    }
                    break;
                case 6:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("type", matcher.group(2))
                            .append("message", matcher.group(3));
                    break;
                case 7:
                    logDocument.append("timestamp", matcher.group(1))
                            .append("server", matcher.group(2))
                            .append("logLevel", matcher.group(3))
                            .append("message", matcher.group(4));
                    break;
            }
            return logDocument;
        } else {
            // If no pattern matches, store the entire line in a "raw" field
            logDocument.append("raw", line);
            return logDocument;
        }
    }

    private static Pair<Integer, Matcher> findPattern(String line){
        Pattern[] patterns = new Pattern[]{
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\d+\\.\\d+\\.\\d+\\.\\d+) (\\w+) (\\S+) (\\d+) (\\w+) (\\d+)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\w+) (\\w+) (\\w+)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\w+) (\\w+) (\\w+) IP:(\\d+\\.\\d+\\.\\d+\\.\\d+)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\d+) (\\d+) (\\w+) (\\d+) (success|failed)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\d+) (\\d+) (\\w+) (\\d+)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (\\d+) (\\d+) (\\w+) (\\d+) (.+?)(?: (error))?$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z) (.+?) (.+)$"),
                Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2},\\d{3}) ([^ ]+) ([^ ]+) (.+)$"),

        };
        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(line);
            if (matcher.matches()) {
                return Pair.of(i, matcher);
            }
        }
        return null;
    }

    public List<Document> getLogs(String userId) {
        List<Document> hebergements = new ArrayList<>();
        MongoClient mongoClient = MongoDBManager.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("monitoring");
        Set<String> uniqueLogfiles = new HashSet<>();
        try {
            // Récupérer la collection "hebergement" depuis la base de données
            MongoCollection<Document> hebergementCollection = database.getCollection("userLog");

            Document query = new Document("userId", userId);
            MongoCursor<Document> cursor = hebergementCollection.find(query).iterator();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String logfile = document.getString("logfile");

                // Check if this logfile is already in the set
                if (!uniqueLogfiles.contains(logfile)) {
                    Document query1 = new Document("userId", userId).append("logfile",logfile);
                    MongoCursor<Document> cursor1 = hebergementCollection.find(query1).iterator();
                    int count=0;
                    while (cursor1.hasNext()){
                        Document document1 = cursor1.next();
                        ObjectId objectId = document1.getObjectId("monitoringDocumentId");
                        // Récupérer la collection "hebergement" depuis la base de données
                        count = getDocs(objectId,count);
                    }
                    document.append("CountDocs",count);
                    hebergements.add(document);
                    uniqueLogfiles.add(logfile); // Add logfile to the set to mark it as seen
                }
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hebergements;
    }

    public int getCountLogs(String userId) {
        int count =0;
        MongoClient mongoClient = MongoDBManager.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("monitoring");
        Set<String> uniqueLogfiles = new HashSet<>();
        try {
            // Récupérer la collection "hebergement" depuis la base de données
            MongoCollection<Document> hebergementCollection = database.getCollection("userLog");

            Document query = new Document("userId", userId);
            MongoCursor<Document> cursor = hebergementCollection.find(query).iterator();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String logfile = document.getString("logfile");

                // Check if this logfile is already in the set
                if (!uniqueLogfiles.contains(logfile)) {
                    count++;
                    uniqueLogfiles.add(logfile); // Add logfile to the set to mark it as seen
                }
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getCountDocs(String userId) {
        int count =0;
        MongoClient mongoClient = MongoDBManager.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("monitoring");
        try {
            // Récupérer la collection "hebergement" depuis la base de données
            MongoCollection<Document> hebergementCollection = database.getCollection("userLog");

            Document query = new Document("userId", userId);
            MongoCursor<Document> cursor = hebergementCollection.find(query).iterator();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                ObjectId objectId = document.getObjectId("monitoringDocumentId");
                // Récupérer la collection "hebergement" depuis la base de données
                count = getDocs(objectId,count);
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getDocs(ObjectId id,int count) {
        MongoClient mongoClient = MongoDBManager.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("monitoring");
        try {
            // Récupérer la collection "hebergement" depuis la base de données
            MongoCollection<Document> hebergementCollection = database.getCollection("monitoring");

            Document query = new Document("_id", id);
            Document userDocument = hebergementCollection.find(query).first();
            if (hebergementCollection.countDocuments(query) == 1) {
                count ++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public List<Document> getVisDocs(String logfile,String userId) {
        List<Document> hebergements = new ArrayList<>();
        MongoClient mongoClient = MongoDBManager.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("monitoring");
        try {
            // Récupérer la collection "hebergement" depuis la base de données
            MongoCollection<Document> hebergementCollection = database.getCollection("userLog");

            Document query = new Document("userId", userId).append("logfile",logfile);
            MongoCursor<Document> cursor = hebergementCollection.find(query).iterator();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                ObjectId objectId = document.getObjectId("monitoringDocumentId");
                MongoCollection<Document> MonitoringCollection = database.getCollection("monitoring");

                Document query1 = new Document("_id", objectId);
                Document monitoringDocument = MonitoringCollection.find(query1).first();
                if (MonitoringCollection.countDocuments(query1) == 1) {
                    hebergements.add(monitoringDocument);
                }
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hebergements;
    }

}
