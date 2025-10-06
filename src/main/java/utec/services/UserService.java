package utec.services;

import org.json.JSONObject;
import utec.dtos.UserDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<String, UserDTO> users = new ConcurrentHashMap<>();

    public UserDTO registerUser(JSONObject json) throws Exception {
        if (!json.has("firstName") || !json.has("lastName") ||
                !json.has("email") || !json.has("password")) {
            throw new Exception("Missing required fields");
        }

        String firstName = json.getString("firstName").trim();
        String lastName = json.getString("lastName").trim();
        String email = json.getString("email").trim();
        String password = json.getString("password").trim();

        if (existsEmail(email)) throw new Exception("Email already exists");

        UserDTO user = new UserDTO();
        user.id = UUID.randomUUID().toString();
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.password = password; // <- sin modificación ni cifrado

        users.put(user.id, user);
        return user;
    }

    public boolean existsEmail(String email) {
        return users.values().stream().anyMatch(u -> u.email.equalsIgnoreCase(email));
    }

    public UserDTO findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email.equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    public UserDTO getUserById(String id) {
        return users.get(id);
    }

    public void clear() {
        users.clear();
    }
}
