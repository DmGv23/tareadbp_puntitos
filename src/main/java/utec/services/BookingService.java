package utec.services;

import utec.dtos.BookingDTO;
import utec.dtos.FlightDTO;
import utec.dtos.UserDTO;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BookingService {
    private final Map<String, BookingDTO> bookings = new ConcurrentHashMap<>();
    private final FlightService flightService;
    private final UserService userService;

    public BookingService(FlightService flightService, UserService userService) {
        this.flightService = flightService;
        this.userService = userService;
    }

    public BookingDTO createBooking(String flightId, String userId) throws Exception {
        FlightDTO flight = flightService.getFlightById(flightId);
        UserDTO user = userService.getUserById(userId);

        if (flight == null) throw new Exception("Flight not found");
        if (user == null) throw new Exception("User not found");

        if (flight.availableSeats <= 0) throw new Exception("No seats available");

        flight.availableSeats = flight.availableSeats - 1;

        BookingDTO booking = new BookingDTO();
        booking.id = UUID.randomUUID().toString();
        booking.bookingDate = Instant.now().toString();
        booking.flightId = flight.id;
        booking.flightNumber = flight.flightNumber;
        booking.customerId = user.id;
        booking.customerFirstName = user.firstName;
        booking.customerLastName = user.lastName;

        bookings.put(booking.id, booking);
        return booking;
    }

    public BookingDTO getBooking(String id) {
        return bookings.get(id);
    }

    public void clear() {
        bookings.clear();
    }
}
