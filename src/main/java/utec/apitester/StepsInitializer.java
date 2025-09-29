package utec.apitester;

import org.json.JSONObject;
import utec.apitester.utils.DateUtils;
import utec.apitester.utils.MockUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class StepsInitializer {
    // Use LinkedHashMap to keep the order
    private final HashMap<String, StepGroup> stepGroups = new LinkedHashMap<>();

    public HashMap<String, StepGroup> initialize() {
        stepGroups.forEach((s, stepGroup) -> stepGroup.clear());
        stepGroups.clear();
        addGroupCreateFlight();
        addGroupRegisterUser();
        addGroupAuthToken();
        addGroupSearchFlight();
        addGroupSearchFlightNiceToHave();
        addGroupBookFlight();
        addGroupBookFlightNiceToHave();

        // always add to the end
        addGroupCreateManyFlightNiceToHave();
        return stepGroups;
    }

    private StepGroup addGroup(String groupName, Double score, Boolean mustHave) {
        var found = stepGroups.get(groupName);
        if (found != null) {
            throw new Error(String.format("Group repeated: %s", groupName));
        }

        var newGroup = new StepGroup(groupName, score, mustHave);
        stepGroups.put(groupName, newGroup);
        return newGroup;
    }

    private Function<JSONObject, Exception> getExpectOneFieldLambda(String fieldName) {
        return (jo) -> {
            if (jo.get(fieldName) == null) {
                return new Exception("Expected Response: { id: \"new id\" }");
            }

            return null;
        };
    }

    private void addStep(String groupName, Step step) {
        var group = stepGroups.get(groupName);

        var found = group.getSteps().get(step.getName());
        if (found != null) {
            throw new Error(String.format("Group %s, Step repeated: %s", group, step.getName()));
        }

        group.addStep(step);
    }

    private void addGroupCreateFlight() {
        var urlPath = "/flights/create";
        var group = addGroup("CREATE_FLIGHT", 0.2, true);

        addStep(group.getName(),
                Step.create("FLIGHT_MANDATORY_FIELDS",
                            "Test if all mandatory fields are validated (airlineName, flightNumber, estDepartureTime, estArrivalTime, availableSeats)",
                            new StepRequest("POST", urlPath, "{}"),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_NUMBER_FORMAT",
                            "Test if the flight number format is validated. Expected RegEx: ^[A-Z]{2,3}[0-9]{3}$",
                            new StepRequest("POST",
                                            "/flights/create",
                                            Stream.of("_", "-", "$", "912AA")
                                                  .map((x) -> MockUtils.mockFlight("American Airlines", x).toString())
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_AVAILABLE_SEATS_MORE_THAN_ZERO",
                            "Test if the available seats are more than zero",
                            new StepRequest("POST",
                                            "/flights/create",
                                            MockUtils.mockFlight("American Airlines", "AA448")
                                                     .put("availableSeats", 0)
                                                     .toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_AA448", "Test if the flight can be created", new StepRequest("POST",
                                                                                                       urlPath,
                                                                                                       // 5 days from now
                                                                                                       MockUtils.mockFlight(
                                                                                                               "American Airlines",
                                                                                                               "AA448",
                                                                                                               3,
                                                                                                               700,
                                                                                                               3,
                                                                                                               1400
                                                                                                       ).toString()
                            ), new StepOptions(true, false, true, true), new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );

        addStep(group.getName(),
                Step.create("TEST_UNIQUE_AA448",
                            "Test if the flight number is unique",
                            new StepRequest("POST",
                                            urlPath,
                                            MockUtils.mockFlight("XX", "AA448", 0, 0, 0, 0).toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_AA754", "Test if the flight can be created", new StepRequest("POST",
                                                                                                       urlPath,
                                                                                                       // 5 days from now
                                                                                                       MockUtils.mockFlight(
                                                                                                               "American Airlines",
                                                                                                               "AA754",
                                                                                                               5,
                                                                                                               700,
                                                                                                               5,
                                                                                                               1400
                                                                                                       ).toString()
                            ), new StepOptions(true, false, true, true), new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );

        // overlaps with AA448
        var jo = MockUtils.mockFlight("LATAM Airlines", "LA876", 3, 900, 3, 1500);
        addStep(group.getName(),
                Step.create("TEST_SUCCESS_LA876",
                            "Test if the flight can be created",
                            new StepRequest("POST", urlPath, jo.toString()),
                            new StepOptions(true, false, false, true),
                            new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );

        jo = MockUtils.mockFlight("Delta Airlines", "DL116", 8, 800, 8, 1900);
        addStep(group.getName(),
                Step.create("TEST_SUCCESS_DL116",
                            "Test if the flight can be created",
                            new StepRequest("POST", urlPath, jo.toString()),
                            new StepOptions(true, false, false, true),
                            new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );

        jo = MockUtils.mockFlight("Spirit Airlines", "NK962", -5, 800, -5, 1900);
        addStep(group.getName(),
                Step.create("TEST_SUCCESS_PAST_NK962",
                            "Test if the flight can be created",
                            new StepRequest("POST", urlPath, jo.toString()),
                            new StepOptions(true, false, false, true),
                            new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );
    }

    private void addGroupRegisterUser() {
        var urlPath = "/users/register";
        var group = addGroup("REGISTER_USER", 0.2, true);

        addStep(group.getName(),
                Step.create("REGISTER_MANDATORY_FIELDS",
                            "Test if all mandatory fields are validated (firstName, lastName, email, password)",
                            new StepRequest("POST", urlPath, "{}"),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_EMAIL_FORMAT",
                            "Test if the email format is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            Stream.of("_", "-", "$", "abc@def", "@def", "abc")
                                                  .map((email) -> MockUtils.mockUser("John", "Doe", email, "1")
                                                                           .toString())
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        List.of("first", "last").forEach((fieldPrefix) -> {
            addStep(group.getName(),
                    Step.create(String.format("TEST_%s_NAME_FORMAT", fieldPrefix.toUpperCase()),
                                String.format("Test if the %s name format is validated", fieldPrefix),
                                new StepRequest("POST", urlPath, Stream.of("", "a", "$", "-", "1").map((x) -> {
                                    var fieldName = fieldPrefix + "Name";
                                    var jo = MockUtils.mockUser("X", "X", "johndoe@gmail.com", "1");
                                    jo.put(fieldName, x);
                                    return jo.toString();
                                }).toList()
                                ),
                                new StepOptions(true, false, false),
                                new StepExpected(400)
                    )
            );
        });

        addStep(group.getName(),
                Step.create("TEST_PASSWORD_FORMAT",
                            "Test if the password format is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            Stream.of("", "a", "aaaaaaaa", "aaaaaaaaa", "12345678", "123456789")
                                                  .map((pwd) -> MockUtils.mockUser("John",
                                                                                   "Doe",
                                                                                   "johndoe@gmail.com",
                                                                                   pwd
                                                  ).toString())
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_JOHN_DOE",
                            "Test if the user can be registered",
                            new StepRequest("POST",
                                            urlPath,
                                            MockUtils.mockUser("John", "Doe", "johndoe@gmail.com", "1234ABCD")
                                                     .toString()
                            ),
                            new StepOptions(true, false, true, true),
                            new StepExpected(201, getExpectOneFieldLambda("id"))
                )
        );

        addStep(group.getName(),
                Step.create("TEST_UNIQUE_JOHN_DOE",
                            "Test if the email is unique",
                            new StepRequest("POST",
                                            urlPath,
                                            MockUtils.mockUser("John", "Doe", "johndoe@gmail.com", "1234ABCD")
                                                     .toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );
    }

    private void addGroupAuthToken() {
        var urlPath = "/auth/login";
        var group = addGroup("AUTH_LOGIN", 0.2, true);

        addStep(group.getName(),
                Step.create("LOGIN_MANDATORY_FIELDS",
                            "Test if all mandatory fields are validated (email, password)",
                            new StepRequest("POST",
                                            urlPath,
                                            Arrays.asList("{}",
                                                          new JSONObject().put("email", "whatever").toString(),
                                                          new JSONObject().put("password", "whatever").toString()
                                            )
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_UNKNOWN_USER",
                            "Test if unknown user is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            new JSONObject().put("email", "whatever")
                                                            .put("password", "whatever")
                                                            .toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_WRONG_PASSWORD",
                            "Test if wrong password is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            new JSONObject().put("email", "johndoe@gmail.com")
                                                            .put("password", "whatever")
                                                            .toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("LOGIN_SUCCESS",
                            "Test if login is successful and a token is generated",
                            new StepRequest("POST",
                                            urlPath,
                                            new JSONObject().put("email", "johndoe@gmail.com")
                                                            .put("password", "1234ABCD")
                                                            .toString()
                            ),
                            new StepOptions(true, false, true, true),
                            new StepExpected(200, getExpectOneFieldLambda("token"))
                )
        );
    }

    private void addGroupSearchFlight() {
        var urlPath = "/flights/search";
        var group = addGroup("SEARCH_FLIGHT", 0.4, true);

        addStep(group.getName(),
                Step.create("NUMBER_EXACT_AA448",
                            "Search for flight number exact AA448",
                            new StepRequest("GET", urlPath + "?flightNumber=AA448"),
                            new StepOptions(true, true, true),
                            new StepExpected(200, (jo) -> {
                                var failed = false;

                                if (jo.getJSONArray("items") == null) {
                                    failed = true;
                                } else {
                                    var items = jo.getJSONArray("items");
                                    failed = items.length() != 1;
                                    if (!failed) {
                                        var item = items.getJSONObject(0);
                                        failed = item.get("id") == null;
                                        if (!failed) {
                                            failed = !item.get("flightNumber").equals("AA448");
                                        }
                                    }
                                }

                                return failed ? new Exception("""
                                                                      Expected Result:
                                                                      { items: [ { id, flightNumber: AA448, ...  } ] }
                                                                      """) : null;
                            }
                            )
                )
        );

        // partial flight, partial airline and exact airline, all validate the same
        Function<JSONObject, Exception> validator = (jo) -> {
            var failed = false;

            if (jo.getJSONArray("items") == null) {
                failed = true;
            } else {
                var items = jo.getJSONArray("items");
                failed = items.length() != 2;
                if (!failed) {
                    var item1 = items.getJSONObject(0);
                    var item2 = items.getJSONObject(1);
                    failed = item1.get("id") == null || !item1.get("flightNumber")
                                                              .equals("AA448") || item2.get("id") == null || !item2.get(
                            "flightNumber").equals("AA754");
                }
            }

            return failed ? new Exception("""
                                                  Expected Result:
                                                  { items: [
                                                      { id, flightNumber: AA448, ...  },
                                                      { id, flightNumber: AA754, ...  },
                                                    ] }
                                                  """) : null;
        };

        addStep(group.getName(),
                Step.create("NUMBER_PARTIAL_AA",
                            "Search for flight number partial AA",
                            new StepRequest("GET", urlPath + "?flightNumber=AA"),
                            new StepOptions(true, true, true),
                            new StepExpected(200, validator)
                )
        );

        addStep(group.getName(),
                Step.create("AIRLINE_EXACT_AA",
                            "Search airline exact American Airlines",
                            new StepRequest("GET", urlPath + "?airlineName=American%20Airlines"),
                            new StepOptions(true, true, true),
                            new StepExpected(200, validator)
                )
        );

        addStep(group.getName(),
                Step.create("AIRLINE_PARTIAL_AMERICAN",
                            "Search airline partial American",
                            new StepRequest("GET", urlPath + "?airlineName=American"),
                            new StepOptions(true, true, true),
                            new StepExpected(200, validator)
                )
        );
    }

    private void addGroupSearchFlightNiceToHave() {
        var urlPath = "/flights/search";
        var group = addGroup("SEARCH_FLIGHT_NICE_TO_HAVE", 0.2, false);

        Function<String, Function<JSONObject, Exception>> validator = (String flightNumber) -> {
            return (JSONObject jo) -> {
                var failed = false;

                if (jo.getJSONArray("items") == null) {
                    failed = true;
                } else {
                    var items = jo.getJSONArray("items");
                    failed = items.length() != 1;
                    if (!failed) {
                        var item = items.getJSONObject(0);
                        failed = item.get("id") == null;
                        if (!failed) {
                            failed = !item.get("flightNumber").equals(flightNumber);
                        }
                    }
                }

                return failed ? new Exception(String.format("""
                                                                    Expected Result:
                                                                    { items: [ { flightNumber: %s, ...  } ] }
                                                                    """, flightNumber
                )) : null;
            };
        };

        var dateFrom = DateUtils.newDateFromToday(8, 700);
        addStep(group.getName(),
                // THIS IS THE LATEST FLIGHT
                Step.create("DEPARTURE_DATE_FROM_TARGET_DL116",
                            "Search flight by departure date from",
                            new StepRequest("GET",
                                            urlPath + "?estDepartureTimeFrom=" + URLEncoder.encode(DateUtils.toISO(
                                                    dateFrom), StandardCharsets.UTF_8
                                            )
                            ),
                            new StepOptions(true, true, true),
                            new StepExpected(200, validator.apply("DL116"))
                )
        );

        var dateTo = DateUtils.newDateFromToday(0, 0);
        addStep(group.getName(),
                // THIS IS THE EARLIEST FLIGHT
                Step.create("DEPARTURE_DATE_TO_TARGET_NK962",
                            "Search flight by departure date to",
                            new StepRequest("GET",
                                            urlPath + "?estDepartureTimeTo=" + URLEncoder.encode(DateUtils.toISO(dateTo),
                                                                                                 StandardCharsets.UTF_8
                                            )
                            ),
                            new StepOptions(true, true, true),
                            new StepExpected(200, validator.apply("NK962"))
                )
        );
    }

    private void addGroupBookFlight() {
        var bookPath = "/flights/book";

        var group = addGroup("BOOK_FLIGHT", 0.5, true);

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_BOOK_FLIGHT_AA448",
                            "Test successful booking on flight AA448 by John Doe",
                            new StepRequest("POST",
                                            bookPath,
                                            (responses) -> new JSONObject().put("flightId",
                                                                                responses.get("TEST_SUCCESS_AA448")
                                                                                         .getResponseJSON()
                                                                                         .getString("id")
                                            )
                            ),
                            new StepOptions(true, true, true, true),
                            new StepExpected(200, getExpectOneFieldLambda("id"))
                )
        );

        addStep(group.getName(),
                Step.create("READ_SUCCESS_BOOK_FLIGHT_AA448",
                            "Read successful booking on flight AA448 by John Doe",
                            new StepRequest("GET",
                                            (responses) -> "/flights/book/" + responses.get(
                                                    "TEST_SUCCESS_BOOK_FLIGHT_AA448").getResponseJSON().getString("id"),
                                            ""
                            ),
                            new StepOptions(true, true, false, true),
                            new StepExpected(200, (jo) -> {
                                if (!Stream.of("id",
                                               "bookingDate",
                                               "flightId",
                                               "flightNumber",
                                               "customerId",
                                               "customerFirstName",
                                               "customerLastName",
                                               "estDepartureTime",
                                               "estArrivalTime"
                                ).allMatch(f -> jo.has(f) && jo.get(f) != null)) {
                                    return new Exception(
                                            "Expected Result: { id, bookingDate, flightId, flightNumber, customerId, customerFirstName, customerLastName }");
                                }

                                return null;
                            }
                            )
                )
        );

        addStep(group.getName(),
                Step.create("TEST_OVERBOOK_FLIGHT_AA448",
                            "Test overbooking on flight AA448",
                            new StepRequest("POST",
                                            bookPath,
                                            (responses) -> new JSONObject().put("flightId",
                                                                                responses.get("TEST_SUCCESS_AA448")
                                                                                         .getResponseJSON()
                                                                                         .getString("id")
                                            )
                            ),
                            new StepOptions(true, true, true),
                            new StepExpected(400)
                )
        );
    }

    private void addGroupBookFlightNiceToHave() {
        var bookPath = "/flights/book";

        var group = addGroup("BOOK_FLIGHT_NICE_TO_HAVE", 0.2, false);

        addStep(group.getName(),
                Step.create("TEST_CANNOT_BOOK_FLIGHT_PAST_NK962",
                            "Test cannot book past flight NK962",
                            new StepRequest("POST",
                                            bookPath,
                                            (responses) -> new JSONObject().put("flightId",
                                                                                responses.get("TEST_SUCCESS_PAST_NK962")
                                                                                         .getResponseJSON()
                                                                                         .getString("id")
                                            )
                            ),
                            new StepOptions(true, true, true),
                            new StepExpected(400)
                )
        );

        // this assumes that John Doe has already booked AA448 (must have test)
        addStep(group.getName(),
                Step.create("TEST_CANNOT_BOOK_OVERLAPPING_AA448_LA876",
                            "Test cannot book overlapping flights AA448 LA876",
                            new StepRequest("POST",
                                            bookPath,
                                            (responses) -> new JSONObject().put("flightId",
                                                                                responses.get("TEST_SUCCESS_LA876")
                                                                                         .getResponseJSON()
                                                                                         .getString("id")
                                            )
                            ),
                            new StepOptions(true, true, true),
                            new StepExpected(400)
                )
        );
    }

    private void addGroupCreateManyFlightNiceToHave() {
        // only possible after the group search flight exists
        var group = addGroup("CREATE_MANY_FLIGHT_NICE_TO_HAVE", 0.4, false);

        addStep(group.getName(),
                Step.create("CREATE_MANY_FLIGHT_UNITED",
                            "Create many flights for United Airlines",
                            new StepRequest("POST",
                                            "/flights/create-many",
                                            new JSONObject().put("inputs",
                                                                 Arrays.asList(MockUtils.mockFlight("United Airlines",
                                                                                                    "UA001",
                                                                                                    1,
                                                                                                    0,
                                                                                                    1,
                                                                                                    1
                                                                               ),
                                                                               MockUtils.mockFlight("United Airlines",
                                                                                                    "UA002",
                                                                                                    1,
                                                                                                    2,
                                                                                                    1,
                                                                                                    3
                                                                               ),
                                                                               MockUtils.mockFlight("United Airlines",
                                                                                                    "UA003",
                                                                                                    1,
                                                                                                    3,
                                                                                                    1,
                                                                                                    4
                                                                               )
                                                                 )
                                            ).toString()
                            ),
                            new StepOptions(true, true, true, true),
                            new StepExpected(201)
                )
        );

        addStep(group.getName(),
                Step.create("READ_MANY_FLIGHT_UNITED",
                            "Read the flights that were created (United Airlines)",
                            new StepRequest("GET", "/flights/search?airlineName=United%20Airlines"),
                            new StepOptions(true, true, true, false, 10),
                            new StepExpected(200, (jo) -> {
                                var failed = false;

                                if (jo.getJSONArray("items") == null) {
                                    failed = true;
                                } else {
                                    var items = jo.getJSONArray("items");
                                    failed = items.length() != 3;
                                    if (!failed) {
                                        failed = !items.getJSONObject(0)
                                                       .getString("flightNumber")
                                                       .equals("UA001") && items.getJSONObject(1)
                                                                                .getString("flightNumber")
                                                                                .equals("UA002") && items.getJSONObject(
                                                2).getString("flightNumber").equals("UA003");

                                    }
                                }

                                return failed ? new Exception("""
                                                                      Expected Result:
                                                                      { items: [
                                                                          { id, flightNumber: UA001, ...  },
                                                                          { id, flightNumber: UA002, ...  },
                                                                          { id, flightNumber: UA003, ...  },
                                                                        ] }
                                                                      """) : null;
                            }
                            )
                )
        );

    }
}
