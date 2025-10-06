package utec.services;

public class ServiceRegistry {

    public static final UserService USER = new UserService();
    public static final AuthService AUTH = new AuthService(USER);
    public static final FlightService FLIGHT = new FlightService();
    public static final BookingService BOOKING = new BookingService(FLIGHT, USER);

    private ServiceRegistry() {

    }

    public static void clearAll() {
        USER.clear();
        AUTH.clear();
        FLIGHT.clear();
        BOOKING.clear();
    }
}
