package utec.dto;

public class NewFlightRequestDTO {
    public String airlineName;
    public String flightNumber;
    public String estDepartureTime; // en ISO-8601 (Instant)
    public String estArrivalTime;
    public int availableSeats;
}
