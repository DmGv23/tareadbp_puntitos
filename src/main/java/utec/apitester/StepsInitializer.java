package utec.apitester;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    private void addStep(String groupName, Step step) {
        var group = stepGroups.get(groupName);

        var found = group.getSteps().get(step.getName());
        if (found != null) {
            throw new Error(String.format("Group %s, Step repeated: %s", group, step.getName()));
        }

        group.addStep(step);
    }

    private JSONObject mockGoodFlight(String flightNumber) {
        return new JSONObject().put("airlineName", "American Airlines")
                               .put("flightNumber", flightNumber)
                               .put("estDepartureTime", DateUtils.newDateFromToday(5, 900))
                               .put("estArrivalTime", DateUtils.newDateFromToday(5, 1400))
                               .put("availableSeats", 50);
    }

    private void addGroupCreateFlight() {
        var urlPath = "/flights/create";
        var group = addGroup("CREATE FLIGHT", 0.2, true);

        addStep(group.getName(),
                Step.create("MANDATORY_FIELDS",
                            "Test Mandatory Fields",
                            "Test if all mandatory fields are validated (airlineName, flightNumber, estDepartureTime, estArrivalTime, availableSeats)",
                            new StepRequest("POST", urlPath, "{}"),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_NUMBER_FORMAT",
                            "Test Flight Number Format",
                            "Test if the flight number format is validated. Expected RegEx: ^[A-Z]{2,3}[0-9]{3}$",
                            new StepRequest("POST",
                                            "/flights/create",
                                            Stream.of("_", "-", "$", "912AA")
                                                  .map((x) -> mockGoodFlight(x).toString())
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_AVAILABLE_SEATS_MORE_THAN_ZERO",
                            "Test Available Seats More Than Zero",
                            "Test if the available seats are more than zero",
                            new StepRequest("POST",
                                            "/flights/create",
                                            mockGoodFlight("AA448").put("availableSeats", 0).toString()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_AA448",
                            "Test Successful Call",
                            "Test if the flight can be created. Expected Response: { id: \"new id\" }",
                            new StepRequest("POST", urlPath, mockGoodFlight("AA448").toString()),
                            new StepOptions(true, false, true),
                            new StepExpected(201)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_UNIQUE_AA448",
                            "Test Flight Number Unique",
                            "Test if the flight number is unique",
                            new StepRequest("POST", urlPath, mockGoodFlight("AA448").toString()),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );

        //        addStep(group.getName(),
        //                Step.create("TEST_ADD_MANY",
        //                            "Test Flight Number Unique",
        //                            "Test if the flight number is unique",
        //                            new StepRequest("POST",urlPath, mockGoodFlight("AA448").toString()),
        //                            new StepOptions(false, false, false),
        //                            new StepExpected(400)
        //
        //                )
        //        );
    }

    private JSONObject mockGoodUser(String email) {
        return new JSONObject().put("firstName", "John")
                               .put("lastName", "Doe")
                               .put("email", email)
                               .put("password", "1234ABCD");
    }

    private void addGroupRegisterUser() {
        var urlPath = "/users/register";
        var group = addGroup("REGISTER USER", 0.2, true);

        addStep(group.getName(),
                Step.create("MANDATORY_FIELDS",
                            "Test Mandatory Fields",
                            "Test if all mandatory fields are validated (firstName, lastName, email, password)",
                            new StepRequest("POST", urlPath, "{}"),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_EMAIL_FORMAT",
                            "Test Email Format",
                            "Test if the email format is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            Stream.of("_", "-", "$", "abc@def", "@def", "abc")
                                                  .map((x) -> mockGoodUser(x).toString())
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        List.of("first", "last").forEach((fieldPrefix) -> {
            addStep(group.getName(),
                    Step.create(String.format("TEST_%s_NAME_FORMAT", fieldPrefix.toUpperCase()),
                                String.format("Test %s Name Format",
                                              fieldPrefix.substring(0, 1).toUpperCase() + fieldPrefix.substring(1)
                                ),
                                String.format("Test if the first name format is validated", fieldPrefix),
                                new StepRequest("POST", urlPath, Stream.of("", "a", "$", "-", "1").map((x) -> {
                                    var fieldName = fieldPrefix + "Name";
                                    var jo = mockGoodUser(x);
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
                            "Test Password Format",
                            "Test if the password format is validated",
                            new StepRequest("POST",
                                            urlPath,
                                            Stream.of("", "a", "aaaaaaaa", "aaaaaaaaa", "12345678", "123456789")
                                                  .map((x) -> {
                                                      var jo = mockGoodUser(x);
                                                      jo.put("password", x);
                                                      return jo.toString();
                                                  })
                                                  .toList()
                            ),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
                )
        );

        addStep(group.getName(),
                Step.create("TEST_SUCCESS_JOHN_DOE",
                            "Test Successful Call",
                            "Test if the user can be registered. Expected Response: { id: \"new id\" }",
                            new StepRequest("POST", urlPath, mockGoodUser("johndoe@gmail.com").toString()),
                            new StepOptions(true, false, true),
                            new StepExpected(201)

                )
        );

        addStep(group.getName(),
                Step.create("TEST_UNIQUE_JOHN_DOE",
                            "Test Email Unique",
                            "Test if the email is unique",
                            new StepRequest("POST", urlPath, mockGoodUser("johndoe@gmail.com").toString()),
                            new StepOptions(true, false, false),
                            new StepExpected(400)

                )
        );
    }

    private void addGroupAuthToken() {
        var urlPath = "/auth/login";
        var group = addGroup("AUTH LOGIN", 0.2, true);

        addStep(group.getName(),
                Step.create("MANDATORY_FIELDS",
                            "Test Mandatory Fields",
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
                Step.create("UNKNOWN USER",
                            "Test For Unknown User",
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
                Step.create("WRONG PASSWORD",
                            "Test For Wrong Password",
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
                Step.create("LOGIN SUCCESS",
                            "Test For Login Success",
                            "Test if login is successful and a token is generated",
                            new StepRequest("POST",
                                            urlPath,
                                            new JSONObject().put("email", "johndoe@gmail.com")
                                                            .put("password", "1234ABCD")
                                                            .toString()
                            ),
                            new StepOptions(true, false, true),
                            new StepExpected(200)
                )
        );
    }

}
