package utec.models;

import java.time.Instant;
import java.util.UUID;

public class Booking {
    public String id = UUID.randomUUID().toString();
    public Instant bookingDate = Instant.now();
    public String flightId;
    public String flightNumber;
    public String customerId;
    public String customerFirstName;
    public String customerLastName;
}
