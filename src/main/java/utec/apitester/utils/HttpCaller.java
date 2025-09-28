package utec.apitester.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class HttpCaller {
    private final Logger logger = LoggerFactory.getLogger(HttpCaller.class);
    private final String baseUrl;
    private String bearerToken;

    public HttpCaller(String baseUrl) {
        this.baseUrl = baseUrl;
        logger.debug("Set baseUrl: {}", baseUrl);
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
        logger.debug("Set bearerToken: {}", bearerToken);
    }

    private HttpRequest.Builder createBuilder(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        logger.debug("building request: {}", path);
        var builder = HttpRequest.newBuilder()
                                 .uri(URI.create(baseUrl + path))
                                 .timeout(Duration.of(10, ChronoUnit.SECONDS));

        builder.header("Content-Type", "application/json");

        if (bearerToken != null) {
            builder.header("Authorization", "Bearer " + bearerToken);
        }

        return builder;
    }

    public HttpResponse<String> httpAny(String method, String path, String body) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.Builder builder = switch (method) {
                case "GET" -> createBuilder(path).GET();
                case "POST" -> createBuilder(path).POST(HttpRequest.BodyPublishers.ofString(body));
                case "PUT" -> createBuilder(path).PUT(HttpRequest.BodyPublishers.ofString(body));
                case "DELETE" -> createBuilder(path).DELETE();
                default -> throw new Exception("Unsupported HTTP method: " + method);
            };

            logger.debug("starting request: {} {} {}", method, path, body);
            var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            logger.debug("finished request: {} {} -> {} {}", method, path, response.statusCode(), response.body());
            return response;
        }
    }
}
