package com.example.demo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.Serializable;

public class MongoDBManager implements Serializable {
    private static final String connectionString = "mongodb://localhost:27017";
    private static MongoClient mongoClient;

    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            ConnectionString connString = new ConnectionString(connectionString);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .build();
            mongoClient = MongoClients.create(settings);
        }
        return mongoClient;
    }

    public static void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public MongoCollection<Document> getCollection(String databaseName, String collectionName) {
        MongoDatabase database = getMongoClient().getDatabase(databaseName);
        return database.getCollection(collectionName);
    }
}
