package utec.services;

import org.json.JSONObject;
import utec.dtos.FlightDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlightService {
    private final Map<String, FlightDTO> flights = new ConcurrentHashMap<>();

    public FlightDTO createFlight(JSONObject json) {
        FlightDTO flight = new FlightDTO();
        flight.id = UUID.randomUUID().toString();
        flight.airlineName = json.getString("airlineName");
        flight.flightNumber = json.getString("flightNumber");
        flight.estDepartureTime = json.get("estDepartureTime").toString();
        flight.estArrivalTime = json.get("estArrivalTime").toString();
        flight.availableSeats = json.getInt("availableSeats");

        flights.put(flight.id, flight);
        return flight;
    }

    public boolean existsFlightNumber(String flightNumber) {
        return flights.values().stream().anyMatch(f -> f.flightNumber.equals(flightNumber));
    }

    public List<FlightDTO> searchFlights(String flightNumber, String airlineName) {
        return flights.values().stream()
                .filter(f -> {
                    boolean matchesFlightNumber = flightNumber == null || flightNumber.isEmpty() ||
                            f.flightNumber.toUpperCase().contains(flightNumber.toUpperCase());
                    boolean matchesAirlineName = airlineName == null || airlineName.isEmpty() ||
                            f.airlineName.toUpperCase().contains(airlineName.toUpperCase());

                    // Si ambos parámetros están presentes, ambos deben coincidir
                    // Si solo uno está presente, solo ese debe coincidir
                    if (flightNumber != null && !flightNumber.isEmpty() && airlineName != null && !airlineName.isEmpty()) {
                        return matchesFlightNumber && matchesAirlineName;
                    } else if (flightNumber != null && !flightNumber.isEmpty()) {
                        return matchesFlightNumber;
                    } else if (airlineName != null && !airlineName.isEmpty()) {
                        return matchesAirlineName;
                    } else {
                        // Si no hay parámetros de búsqueda, devolver todos
                        return true;
                    }
                })
                .sorted((f1, f2) -> f1.flightNumber.compareToIgnoreCase(f2.flightNumber))
                .toList();
    }

    public List<FlightDTO> searchFlights(String flightNumber) {
        return searchFlights(flightNumber, null);
    }

    public FlightDTO getFlightById(String id) {
        return flights.get(id);
    }

    public void clear() {
        flights.clear();
    }
}
