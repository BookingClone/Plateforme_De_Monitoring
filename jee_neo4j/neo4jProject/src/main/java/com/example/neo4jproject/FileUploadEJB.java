package com.example.neo4jproject;

import com.example.neo4jproject.DB.MongoDBManager;
import com.example.neo4jproject.DB.Neo4jConnexion;
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
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.neo4j.driver.Values.parameters;


@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@Named
public class FileUploadEJB {
    private Neo4jConnexion neo4jConnexion = new Neo4jConnexion();
    private Driver driver=null;
    public void uploadFile(Part uploadedFile, String userId, String userEmail) {
        if (uploadedFile != null) {
            // Obtain the name of the uploaded file
            String fileName = getFileName(uploadedFile);

            try {

                driver = neo4jConnexion.getDriver();
                Session session = driver.session();
                String query = "CREATE (f:File) SET f.fileName = $fileName RETURN id(f) as fileId, f";
                Result result = session.run(query, parameters( "fileName", fileName));
                String fileId = null;
                if (result.hasNext()) {
                    // Le nœud existe, vous pouvez récupérer ses données
                    Record record = result.next();
                    fileId = String.valueOf(record.get("fileId"));
                }

                Session sessionRelation = driver.session();
                String queryRelation = "MATCH (u:User), (f:File) WHERE ID(u) = $userId AND ID(f) = $fileId CREATE (u)-[:UPLOAD]->(f)";
                Result resultRelation = sessionRelation.run(queryRelation, parameters( "userId",Long.parseLong(userId)  , "fileId", Long.parseLong(fileId)));
                // Read the file and insert into MongoDB
                try (BufferedReader br = new BufferedReader(new InputStreamReader(uploadedFile.getInputStream()))) {
                    String line;
                    int insertedCount = 0;
                    int failedCount = 0;

                    while ((line = br.readLine()) != null) {
                        String lineId = parseLogLine(line);
                        System.out.println("lineId "+lineId);
                        if (lineId != null) {
                            // Insert the log document into "monitoring" collection
                            Session sessionLine = driver.session();
                            String queryLine = "MATCH (f:File), (l:LineLog) WHERE ID(f) =  $fileId AND ID(l) = $lineId CREATE (f)-[:CONTAIN]->(l)";
                            Result resultLine = sessionLine.run(queryLine, parameters( "fileId", Long.parseLong(fileId), "lineId", Long.parseLong(lineId)));

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

    public void deletedFile(String fileName, String userEmail){
        driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String query = "MATCH (u:User {email: $userEmail}) MATCH (f:File {fileName: $fileName})<-[:UPLOAD]-(u) OPTIONAL MATCH (f)-[:CONTAIN]->(l:LineLog) DETACH DELETE f, l;";
        Result result = session.run(query, parameters( "userEmail", userEmail,"fileName", fileName));
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

    private static String parseLogLine(String line) {
        Neo4jConnexion neo4jConnexion = new Neo4jConnexion();
        Driver driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String queryLine=null;
        Result result=null;
        Pair<Integer, Matcher> patternAndMatcher = findPattern(line);

        if (patternAndMatcher != null) {
            int patternIndex = patternAndMatcher.getLeft();
            Matcher matcher = patternAndMatcher.getRight();

            switch (patternIndex) {
                case 0:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.ipAddress = $ipAddress, " +
                            "l.method = $method, " +
                            "l.request = $request, " +
                            "l.statusCode = $statusCode, " +
                            "l.response = $response, " +
                            "l.duration = $duration "+
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                                    "ipAddress", matcher.group(2),
                                    "method", matcher.group(3),
                                    "request", matcher.group(4),
                                    "statusCode", matcher.group(5),
                                    "response", matcher.group(6),
                                    "duration", matcher.group(7)
                    ));

                    break;
                case 1:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.username = $username, " +
                            "l.password = $password, " +
                            "l.authentificationStatus = $authentificationStatus " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "username", matcher.group(2),
                            "password", matcher.group(3),
                            "authentificationStatus", matcher.group(4)
                    ));
                    break;
                case 2:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.username = $username, " +
                            "l.password = $password, " +
                            "l.authentificationStatus = $authentificationStatus, " +
                            "l.ipAddress = $ipAddress " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "username", matcher.group(2),
                            "password", matcher.group(3),
                            "authentificationStatus", matcher.group(4),
                            "ipAddress", matcher.group(4)
                    ));
                    break;
                case 3:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.sessionId = $sessionId, " +
                            "l.userId = $userId, " +
                            "l.action = $action, " +
                            "l.duration = $duration, " +
                            "l.status = $status " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "sessionId", matcher.group(2),
                            "userId", matcher.group(3),
                            "action", matcher.group(4),
                            "duration", matcher.group(5),
                            "status", matcher.group(6)
                    ));
                    break;
                case 4:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.sessionId = $sessionId, " +
                            "l.userId = $userId, " +
                            "l.action = $action, " +
                            "l.duration = $duration " +
                            " RETURN id(l) as lineId ";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "sessionId", matcher.group(2),
                            "userId", matcher.group(3),
                            "action", matcher.group(4),
                            "duration", matcher.group(5)
                    ));
                    break;
                case 5:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.sessionId = $sessionId, " +
                            "l.userId = $userId, " +
                            "l.action = $action, " +
                            "l.duration = $duration, " +
                            "l.path = $path " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "sessionId", matcher.group(2),
                            "userId", matcher.group(3),
                            "action", matcher.group(4),
                            "duration", matcher.group(5),
                            "path", matcher.group(6)
                    ));

//                    if (matcher.group(7) != null && matcher.group(7).equals("error")) {
//                        logDocument.append("status", "error");
//                    }
                    break;
                case 6:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.type = $type, " +
                            "l.message = $message " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "type", matcher.group(2),
                            "message", matcher.group(3)
                    ));
                    break;
                case 7:
                    queryLine = "CREATE (l:LineLog) SET " +
                            "l.timestamp = $timestamp, " +
                            "l.server = $server, " +
                            "l.logLevel = $logLevel, " +
                            "l.message = $message " +
                            " RETURN id(l) as lineId";
                    result = session.run(queryLine, parameters(
                            "timestamp", matcher.group(1),
                            "server", matcher.group(2),
                            "logLevel", matcher.group(3),
                            "message", matcher.group(4)
                    ));
                    break;
            }

        } else {
            // If no pattern matches, store the entire line in a "raw" field
            queryLine = "CREATE (l:LineLog) SET " +
                    "l.line = $line " +
                    " RETURN id(l) as lineId";
            result = session.run(queryLine, parameters(
                    "line", line
            ));

        }
        String lineId=null;
        if (result.hasNext()) {
            // Le nœud existe, vous pouvez récupérer ses données
            Record record = result.next();
            lineId = String.valueOf(record.get("lineId"));
        }
        return  lineId;
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

    public List<Document> getLogs(String userEmail) {
        List<Document> logs = new ArrayList<>();
        driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String queryLog = "MATCH (u:User {email: $userEmail})-[:UPLOAD]->(f:File) RETURN f.fileName as fileName";
        Result resultLog = session.run(queryLog, parameters("userEmail", userEmail));

        while (resultLog.hasNext()) {
            Document document = new Document();
            Record record = resultLog.next();
            String fileName = record.get("fileName").asString();
            document.append("logfile",fileName);

            Session sessionDocs = driver.session();
            String queryDocs = "MATCH (f:File {fileName: $fileName})-[:CONTAIN]->(l:LineLog) RETURN COUNT(l) as DocsCount";
            Result resultDocs = sessionDocs.run(queryDocs, parameters("fileName", fileName));
            if (resultDocs.hasNext()) {
                Record recordDocs = resultDocs.next();
                    long count = recordDocs.get("DocsCount").asLong();
                    System.out.println("Nombre de fichiers en relation avec l'utilisateur : " + count);
                    document.append("CountDocs",count);
            }

            logs.add(document);
        }
        return logs;
    // Maintenant, fileNames contient la liste des noms de fichiers en relation avec l'utilisateur

    }

    public long getCountLogs(String userEmail) {
        long count =0;
        driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String query = "MATCH (u:User {email: $userEmail})-[:UPLOAD]->(f:File) RETURN COUNT(f) as fileCount";
        Result result = session.run(query, parameters("userEmail", userEmail));

        if (result.hasNext()) {
            Record record = result.next();
            count = record.get("fileCount").asLong();
            System.out.println("Nombre de fichiers en relation avec l'utilisateur : " + count);
        }
        return count;
    }

    public long getCountDocs(String userEmail) {
        long count =0;
        driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String queryLog = "MATCH (u:User {email: $userEmail})-[:UPLOAD]->(f:File) RETURN f.fileName as fileName";
        Result resultLog = session.run(queryLog, parameters("userEmail", userEmail));

        while (resultLog.hasNext()) {
            Record record = resultLog.next();
            String fileName = record.get("fileName").asString();

            Session sessionDocs = driver.session();
            String queryDocs = "MATCH (f:File {fileName: $fileName})-[:CONTAIN]->(l:LineLog) RETURN COUNT(l) as DocsCount";
            Result resultDocs = sessionDocs.run(queryDocs, parameters("fileName", fileName));
            if (resultDocs.hasNext()) {
                Record recordDocs = resultDocs.next();
                count += recordDocs.get("DocsCount").asLong();
                System.out.println("Nombre de fichiers en relation avec l'utilisateur : " + count);

            }
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

    public List<Document> getVisDocs(String fileName,String userId) {
        List<Document> logs = new ArrayList<>();
        Document document = new Document();
        driver = neo4jConnexion.getDriver();
        Session session = driver.session();
        String query = "MATCH (f:File {fileName: $fileName})-[:CONTAIN]->(l:LineLog) RETURN l";
        Result result = session.run(query, parameters("fileName", fileName));
        while (result.hasNext()) {
            Record record = result.next();
            Node lineLogNode = record.get("l").asNode();

            // Convertir les propriétés du nœud en un Document BSON
            Document lineLogDocument = new Document(lineLogNode.asMap());

            logs.add(lineLogDocument);
        }
        return logs;
    }

}
