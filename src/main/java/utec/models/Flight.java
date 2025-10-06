package utec.models;

import java.time.Instant;
import java.util.UUID;

public class Flight {
    public String id = UUID.randomUUID().toString();
    public String airlineName;
    public String flightNumber;
    public Instant estDepartureTime;
    public Instant estArrivalTime;
    public int availableSeats;
    public int seatsBooked = 0;
}
