package utec.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import utec.apitester.utils.ResponseUtils;
import utec.dtos.BookingDTO;
import utec.dtos.FlightDTO;
import utec.dtos.UserDTO;
import utec.services.ServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class BookingController {

    // POST /flights/book
    public static class BookFlightHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                ResponseUtils.sendError(exchange, 405, "Method not allowed");
                return;
            }

            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                ResponseUtils.sendError(exchange, 401, "Unauthorized");
                return;
            }
            String token = auth.substring(7);
            String userId = ServiceRegistry.AUTH.getUserIdFromToken(token);
            if (userId == null) {
                ResponseUtils.sendError(exchange, 401, "Unauthorized");
                return;
            }

            // Corregir la lectura del body - solo leer una vez
            String body;
            try (Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                body = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            if (body == null || body.isBlank()) {
                ResponseUtils.sendError(exchange, 400, "Missing body");
                return;
            }

            JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (Exception e) {
                ResponseUtils.sendError(exchange, 400, "Invalid JSON");
                return;
            }

            if (!json.has("flightId")) {
                ResponseUtils.sendError(exchange, 400, "Missing flightId");
                return;
            }

            String flightId = json.getString("flightId");

            try {
                BookingDTO booking = ServiceRegistry.BOOKING.createBooking(flightId, userId);

                FlightDTO flight = ServiceRegistry.FLIGHT.getFlightById(flightId);
                UserDTO user = ServiceRegistry.USER.getUserById(userId);

                String firstName = user != null && user.firstName != null ? user.firstName : "";
                String lastName  = user != null && user.lastName  != null ? user.lastName  : "";

                String content = String.format(
                        "Hello %s %s,%n%nYour booking was successful!%n%nThe booking is for flight %s with departure date of %s and arrival date of %s.%n%nThe booking was registered at %s.%n%nBon Voyage!%nFly Away Travel",
                        firstName, lastName,
                        flight != null ? flight.flightNumber : "N/A",
                        flight != null ? flight.estDepartureTime : "N/A",
                        flight != null ? flight.estArrivalTime : "N/A",
                        booking.bookingDate
                );

                try {
                    Files.writeString(Path.of("flight_booking_email_" + booking.id + ".txt"), content);
                } catch (Exception ex) {
                    System.err.println("No se pudo escribir archivo de confirmación: " + ex.getMessage());
                }

                ResponseUtils.sendJSON(exchange, 200, new JSONObject().put("id", booking.id));
            } catch (Exception e) {
                ResponseUtils.sendError(exchange, 400, e.getMessage());
            }
        }
    }

    // GET /flights/book/{id}
    public static class GetBookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ResponseUtils.sendError(exchange, 405, "Method not allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 4 || parts[3].isBlank()) {
                ResponseUtils.sendError(exchange, 400, "Missing booking id");
                return;
            }
            String id = parts[3];

            BookingDTO booking = ServiceRegistry.BOOKING.getBooking(id);
            if (booking == null) {
                ResponseUtils.sendError(exchange, 404, "Booking not found");
                return;
            }

            // Obtener información adicional del vuelo
            FlightDTO flight = ServiceRegistry.FLIGHT.getFlightById(booking.flightId);

            JSONObject resp = new JSONObject()
                    .put("id", booking.id)
                    .put("bookingDate", booking.bookingDate)
                    .put("flightId", booking.flightId)
                    .put("flightNumber", booking.flightNumber)
                    .put("customerId", booking.customerId)
                    .put("customerFirstName", booking.customerFirstName)
                    .put("customerLastName", booking.customerLastName)
                    .put("estDepartureTime", flight != null ? flight.estDepartureTime : "")
                    .put("estArrivalTime", flight != null ? flight.estArrivalTime : "");

            ResponseUtils.sendJSON(exchange, 200, resp);
        }
    }
}