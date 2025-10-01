# Week07 Tester

This project is meant to test week 7 exercise and verify how many goals were successfully accomplished.

## Run

```bash
# Run in a single run
# -u: Set the url to your application to be tested
# -s: (optional) Hit <any key> to pass group by group (hint: use Ctrl+C to quit). If not present, the application will execute in a single run until the end.
# -n: (optional) When set, run the nice-to-have checks. When not set, only the must-have checks will be executed.
$ java -jar week07-tester.jar test -u http://localhost:8080
```

## User Needs

You, as the main development team for Fly Away Travel, are in charge of creating a new API to book air tickets.

The business workflow that must be supported is:

1. The travel agency has to create a flight.

```java
// basic schema
Flight {
    String airlineName;
    String flightNumber;
    Date estDepartureTime;
    Date estArrivalTime;
    Integer availableSeats;
}

// required mapping
@RequestMapping("/flights")
class FlightController {
    // UNPROTECTED
    @PostMapping("/create")
    public ResponseEntity<NewIdDTO> create(@RequestBody NewFlightRequestDTO newFlight) {}
}

// class NewIdDTO {
//    public String id;
// }
```

Must-Have (+0.2)

- Mandatory fields: Airline Name, Flight Number, Estimated Departure Time, Estimated Arrival Time, Available Seats.
- Flight Numbers can be A-Z 0-9, up to 6 characters, e.g. AA984, TAC821.
- Estimated Departure Time < Estimated Arrival Time.
- Available Seats > 0.
- Flight Numbers cannot be repeated.

2. The customer must register as a user.

```java
// basic schema
User {
    String firstName;
    String lastName;
    String email;
    String password;
}

// required mapping
@RequestMapping("/users")
class UserController {
    // UNPROTECTED
    @PostMapping("/register")
    public ResponseEntity<NewIdDTO> register(@RequestBody RegisterUserDTO newUser) {}
}
```

Must-Have (+0.2)

- Mandatory fields: First Name, Last Name, Email Address, Password.
- Email must be a valid email address
- First Name must be at least 1 alpha upper case character (A-Z)
- Last Name must be at least 1 alpha upper case character (A-Z)
- Password must be at least 8 characters long, with at least one letter and one number.

Nice-To-Have (+0.2)

- The response should only be `{ id }` (using DTO).

3. (Must-Have) The customer then must acquire an authentication token. (+0.5)

- Email and password are mandatory
- Must validate for unknown email
- Must validate for wrong password
- Token must be in JWT format

```java
// required mapping
@RequestMapping("/auth")
class AuthController {
    // UNPROTECTED
    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@RequestBody LoginDTO login) {}
}
```

The response should be:

```
  { token: "<jwt>" }
```

- All operations related to the customer must receive this authentication token so the API can tell who is calling it.

4. The customer can search the flights by airline, flight number, or departure date.

Must-Have (+0.4)

- Search by partial flight number
- Search by partial airline name

Nice-Have (+0.2)

- Search from departure date (inclusive)
- Search to departure date (inclusive)

```java
// required mapping
@RequestMapping("/flights")
class FlightController {
    // PROTECTED
    @GetMapping("/search")
    public ResponseEntity<FlightSearchResponseDTO> search(@RequestParam String flightNumber,
                                                          @RequestParam String airlineName,
                                                          @RequestParam String estDepartureTimeFrom,
                                                          @RequestParam String estDepartureTimeTo) {}
}
```

Nice-To-Have (+0.4): ONLY AFTER SEARCH IS IMPLEMENTED.

- Add N flights (Tip: Async operation).

```java
// required mapping
@RequestMapping("/flights")
class FlightController {
    // UNPROTECTED
    @PostMapping("/create-many")
    public ResponseEntity<NewFlightManyResponseDTO> search(@RequestBody NewFlightManyRequestDTO requestDTO) {}
}
```

The tester will wait 10 seconds, and then call GET /flights/search to verify if all were registered.

5. The customer can purchase a flight seat

Must-Have (+0.5)

- Mandatory Input: Flight Id
- The rest of the fields must be calculated: Customer Id, Customer First Name, Customer Last Name, Booking Date
- The flights cannot be oversold.
- There must be a `GET /flight/book/{id}` endpoint that must show the booking information

```
 {
  id,
  bookingDate,
  flightId,
  flightNumber,
  customerId,
  customerFirstName,
  customerLastName
}
```

Nice-To-Have (+0.2)

- The flight cannot be in the past or in transit.
- A customer cannot book a flight that overlaps with another.

```java
// required mapping
@RequestMapping("/flights")
class FlightController {
    // PROTECTED
    @PostMapping("/book")
    public ResponseEntity<NewIdDTO> book(@RequestBody FlightBookRequestDTO requestDTO) {}
}
```

6. (Nice-To-Have) When the customer books their flight successfully, he must receive an email with the confirmation. (
   +0.2)

**INSTRUCTIONS**

- You must use **events**.
- Save a file called `flight_booking_email_${booking_id}.txt` next to where your `week07-tester.jar` is located.
- The email must contain:
    - Customer First Name
    - Customer Last Name
    - Flight Number
    - Est Departure Date (in ISO8601 format 2025-05-01T04:05:00Z)
    - Est Arrival Date (in ISO8601 format 2025-05-01T04:05:00Z)
    - Booking Date and Time (in ISO8601 format 2025-05-01T04:05:00Z)

An example is:

```
Hello {Customer First Name} {Customer Last Name},

Your booking was successful! 

The booking is for flight {Flight Number} with departure date of {Est Departure Date} and arrival date of {Est Arrival Date}.

The booking was registered at {Booking Date Time}.

Bon Voyage!
Fly Away Travel
```

## Technical Requirements/Considerations

- API must be REST.
- The authentication token must be in JWT format.
- The tester will provide exact details about the request and response format. Trial and error will provide you the
  necessary details.
- IMPORTANT: Although the above user-needs do not state any `GET /{entity}/{id}` endpoint, they are required for the
  tester to work.

## CREATE "CLEANUP" CONTROLLER

- Create a special endpoint that will delete all records from the database.
- This will be called by the tester at any point in time.
- The lack of this controller will make all the tests fail.

```java
// BASIC IMPLEMENTATION YOU HAVE TO COMPLETE IT
@RestController("/cleanup")
class CleanupController {
    @DeleteMapping()
    public ResponseEntity cleanup() {
        customerService.deleteAll();
        flightService.deleteAll();
        bookingService.deleteAll();
    }
}
```
