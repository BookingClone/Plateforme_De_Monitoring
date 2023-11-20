package com.example.neo4jproject.DB;

import com.example.neo4jproject.entities.User;
import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;


public class Neo4jConnexion {
    private Driver driver;
    private String uri = "bolt://localhost:7687/Users";
    private String username = "neo4j";
    private String password = "00000000";

    public Neo4jConnexion() {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public Session getSession() {
        return driver.session();
    }
    public Driver getDriver() {return driver;}
    public void close() {
        driver.close();
    }
}
