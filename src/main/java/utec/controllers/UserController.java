package utec.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import utec.dtos.UserDTO;
import utec.services.ServiceRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class UserController {

    // Regex para email
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Regex para password: mínimo 8 caracteres, al menos una minúscula, una mayúscula y un dígito
    private static final Pattern PASSWORD_REGEX =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$");


    // Handler para registrar usuarios
    public static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(body);

            String firstName = json.optString("firstName", "");
            String lastName = json.optString("lastName", "");
            String email = json.optString("email", "");
            String password = json.optString("password", "");

            // Validaciones
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Missing required fields"));
                return;
            }

            if (firstName.length() < 2) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Invalid firstName"));
                return;
            }

            if (lastName.length() < 2) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Invalid lastName"));
                return;
            }

            if (!EMAIL_REGEX.matcher(email).matches()) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Invalid email"));
                return;
            }

            if (!PASSWORD_REGEX.matcher(password).matches()) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Invalid password"));
                return;
            }

            if (ServiceRegistry.USER.existsEmail(email)) {
                sendJSON(exchange, 400, new JSONObject().put("error", "Email already exists"));
                return;
            }

            try {
                UserDTO user = ServiceRegistry.USER.registerUser(json);
                sendJSON(exchange, 201, new JSONObject().put("id", user.id));
            } catch (Exception e) {
                sendJSON(exchange, 400, new JSONObject().put("error", e.getMessage()));
            }
        }
    }

    // Handler para obtener usuario por ID
    public static class GetUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 3) {
                sendJSON(exchange, 400, new JSONObject().put("error", "User ID missing"));
                return;
            }

            String userId = parts[2];
            UserDTO user = ServiceRegistry.USER.getUserById(userId);
            if (user == null) {
                sendJSON(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            JSONObject json = new JSONObject()
                    .put("id", user.id)
                    .put("firstName", user.firstName)
                    .put("lastName", user.lastName)
                    .put("email", user.email);

            sendJSON(exchange, 200, json);
        }
    }

    // Utilidad para responder JSON
    private static void sendJSON(HttpExchange exchange, int status, JSONObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}