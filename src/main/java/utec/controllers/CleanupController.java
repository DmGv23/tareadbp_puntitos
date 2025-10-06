package utec.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import utec.services.ServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CleanupController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // limpiar servicios
        ServiceRegistry.USER.clear();
        ServiceRegistry.FLIGHT.clear();
        ServiceRegistry.BOOKING.clear();
        ServiceRegistry.AUTH.clear();

        JSONObject resp = new JSONObject().put("status", "cleaned");
        byte[] out = resp.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, out.length);
        exchange.getResponseBody().write(out);
        exchange.getResponseBody().close();
    }
}
