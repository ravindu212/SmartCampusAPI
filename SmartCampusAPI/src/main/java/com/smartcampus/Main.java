package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus")
                .register(org.glassfish.jersey.jackson.JacksonFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = startServer();
        System.out.println("Smart Campus API running at " + BASE_URI);
        System.out.println("Press ENTER to stop...");
        System.in.read();
        server.stop();
    }
}