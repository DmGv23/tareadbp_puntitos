package utec.apitester.utils;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResponseUtils {

    public static void sendJSON(HttpExchange exchange, int status, JSONObject json) throws IOException {
        byte[] out = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, out.length);
        exchange.getResponseBody().write(out);
        exchange.getResponseBody().close();
    }

    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        JSONObject error = new JSONObject().put("error", message);
        sendJSON(exchange, status, error);
    }
}
