package utec.apitester.utils;

import org.json.JSONObject;

public class MockUtils {
    public static JSONObject mockFlight(String airlineName, String flightNumber) {
        return mockFlight(airlineName, flightNumber, 5, 900, 5, 1400);
    }

    public static JSONObject mockFlight(String airlineName, String flightNumber, Integer addDepartureDays,
                                        Integer setDepartureTime, Integer addArrivalDays, Integer setArrivalTime) {
        return new JSONObject().put("airlineName", airlineName)
                               .put("flightNumber", flightNumber)
                               .put("estDepartureTime", DateUtils.newDateFromToday(addDepartureDays, setDepartureTime))
                               .put("estArrivalTime", DateUtils.newDateFromToday(addArrivalDays, setArrivalTime))
                               .put("availableSeats", 1);
    }

    public static JSONObject mockUser(String firstName, String lastName, String email, String password) {
        return new JSONObject().put("firstName", firstName)
                               .put("lastName", lastName)
                               .put("email", email)
                               .put("password", password);
    }
}
