package utec.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import utec.services.ServiceRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthController {

    public static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("DEBUG: Login request body: " + body);
                JSONObject json = new JSONObject(body);

                String token = ServiceRegistry.AUTH.login(json);
                System.out.println("DEBUG: Login successful, token generated: " + token);

                JSONObject response = new JSONObject();
                response.put("token", token);

                byte[] respBytes = response.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, respBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(respBytes);
                }

            } catch (Exception e) {
                System.out.println("DEBUG: Login error: " + e.getMessage());
                e.printStackTrace();
                JSONObject error = new JSONObject();
                error.put("error", e.getMessage());
                byte[] respBytes = error.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, respBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(respBytes);
                }
            }
        }
    }
}