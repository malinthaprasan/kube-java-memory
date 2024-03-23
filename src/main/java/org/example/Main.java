package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static Boolean[] bigArray = new Boolean[0];

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // Define a context and an associated handler
        server.createContext("/memory", new MemHandler());
        server.createContext("/allocate-memory", new MyHandler());
        server.createContext("/gc", new GcHandler());
        // Start the server
        server.start();
        System.out.println("Server is listening on port 8000");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Parse the query
            Map<String, String> queryParams = queryToMap(exchange.getRequestURI().getQuery());

            // Get the 'memory' query parameter
            String memoryParam = queryParams.get("mb");
            int memory = 0;
            try {
                // Try to convert 'memory' parameter to int
                memory = Integer.parseInt(memoryParam);
            } catch (NumberFormatException e) {
                // Handle the case where 'memory' is not an integer or not provided
                System.out.println("'memory' parameter is missing or not an integer.");
            }



            try {
                bigArray = new Boolean[memory * 1024 * 1024];
            } catch (Throwable e) {
                System.out.println("Error: " + e);
                throw e;
            }


            // Create a response using the 'memory' parameter
            String response = logMemoryDetails();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            System.out.println(response);
        }

        /**
         * Utility method to convert the query part of the request URI into a Map.
         *
         * @param query The query part of the URI.
         * @return A Map containing the query parameters as keys and their values as map values.
         */
        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] entry = param.split("=");
                    if (entry.length > 1) {
                        result.put(entry[0], entry[1]);
                    } else {
                        result.put(entry[0], "");
                    }
                }
            }
            return result;
        }
    }

    static class GcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Trigger garbage collection
            System.out.println("Garbage collection requested.");
            System.gc();
            System.out.println("Garbage collection suggested.");

            // Send a response back to the client
            String response = logMemoryDetails();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Send a response back to the client
            String response = logMemoryDetails();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static String logMemoryDetails() {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();

        // Calculate the memory values in megabytes
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = allocatedMemory - freeMemory;

        // Manually format as JSON
        String json = "{"
                + "\"maxMemory\": \"" + maxMemory + " MB\", "
                + "\"allocatedMemory\": \"" + allocatedMemory + " MB\", "
                + "\"freeMemory\": \"" + freeMemory + " MB\", "
                + "\"usedMemory\": \"" + usedMemory + " MB\""
                + "}";

        return json;
    }
}
