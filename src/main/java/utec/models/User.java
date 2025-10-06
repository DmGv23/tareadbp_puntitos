package utec.models;

import java.util.UUID;

public class User {
    public String id = UUID.randomUUID().toString();
    public String firstName;
    public String lastName;
    public String email;
    public String passwordHash;
}
