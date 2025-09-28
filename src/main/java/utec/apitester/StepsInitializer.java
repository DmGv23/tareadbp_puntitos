package utec.apitester;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StepsInitializer {
    // Use LinkedHashMap to keep the order
    private final HashMap<String, Step> steps = new LinkedHashMap<>();

    public HashMap<String, Step> initialize() {
        steps.clear();
        addStepsFlight();
        addStepsUsers();
        return steps;
    }

    private void addStep(Step step) {
        if (steps.containsKey(step.getName())) {
            throw new Error("Step repeated: " + step.getName());
        }

        steps.put(step.getName(), step);
    }

    private void addStepsFlight() {
        var namespace = "FLIGHT_REGISTER_";

        addStep(Step.create(namespace + "TEST_MANDATORY_FIELDS",
                            "Testing Register Flight Mandatory Fields",
                            (Step s) -> String.format("""
                                                              Request Sent: %s
                                                              
                                                              Expected HTTP Response: 400
                                                              """, s.getRequest().body()
                            ),
                            new StepRequest("POST", "/flights/register", "{}"),
                            new StepOptions(true, false, false),
                            new StepExpected(400)
        ));

        var wrongNumbers = Arrays.asList("_", "-", "$", "912AA");
        for (int i = 0; i < wrongNumbers.size(); i++) {
            var body = new JSONObject().put("airlineName", "American Airlines")
                                       .put("flightNumber", wrongNumbers.get(i))
                                       .put("estDepartureTime", DateUtils.newDateFromToday(5, 900))
                                       .put("estArrivalTime", DateUtils.newDateFromToday(5, 1400))
                                       .toString();
            int finalI = i;
            addStep(Step.create(namespace + String.format("TEST_NUMBER_FORMAT_%s", i + 1),
                                (Step s) -> "Testing Register Flight Number Format: " + wrongNumbers.get(finalI),
                                (Step s) -> String.format("""
                                                                  Request Sent: %s
                                                                  
                                                                  Expected HTTP Response: 400
                                                                  Expected RegEx for flightNumber: ^[A-Z]{2,3}[0-9]{3}$
                                                                  """, s.getRequest().body()
                                ),
                                new StepRequest("POST", "/flights/register", body),
                                new StepOptions(true, false, false),
                                new StepExpected(400)
            ));
        }

        addStep(Step.create(namespace + "SUCCESS_AA448",
                            "Testing Register Flight Successful",
                            (Step s) -> String.format("""
                                                              Request Body: %s
                                                              
                                                              Expected HTTP Response: 201
                                                              Expected Response: { id: "<new id>" }
                                                              """, s.getRequest().body()
                            ),
                            new StepRequest("POST",
                                            "/flights/register",
                                            new JSONObject().put("airlineName", "American Airlines")
                                                            .put("flightNumber", "AA448")
                                                            .put("estDepartureTime", DateUtils.newDateFromToday(5, 900))
                                                            .put("estArrivalTime", DateUtils.newDateFromToday(5, 1400))
                                                            .toString()
                            ),
                            new StepOptions(true, false, true),
                            new StepExpected(201)
        ));
    }

    private void addStepsUsers() {
        //        var namespace = "USER_REGISTER_";
        //        addStep(Step.create(namespace + "JOHN",
        //                              new StepRequest("POST",
        //                                              "/users/register",
        //                                              new JSONObject().put("firstName", "John")
        //                                                              .put("lastName", "Mayer")
        //                                                              .put("email", "johnmayer@gmail.com")
        //                                                              .toString()
        //                              ),
        //                              new StepOptions(true, false, true),
        //                              new StepExpected(201)
        //        ));
    }
}
