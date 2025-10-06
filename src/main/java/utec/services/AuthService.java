package utec.services;

import org.json.JSONObject;
import utec.apitester.utils.JwtUtil;
import utec.dtos.UserDTO;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final UserService userService;
    private final Map<String, String> tokens = new HashMap<>();

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public String login(JSONObject json) throws Exception {
        if (!json.has("email") || !json.has("password")) {
            throw new Exception("Missing credentials");
        }

        String email = json.getString("email").trim();
        String password = json.getString("password").trim();

        UserDTO user = userService.findByEmail(email);
        if (user == null) {
            throw new Exception("User not found");
        }

        if (!user.password.trim().equals(password)) {
            throw new Exception("Invalid password");
        }

        String token = JwtUtil.generateToken(user.id);
        tokens.put(token, user.id);
        return token;
    }

    public String getUserIdFromToken(String token) {
        return JwtUtil.validateToken(token);
    }

    public void clear() {
        tokens.clear();
    }
}
