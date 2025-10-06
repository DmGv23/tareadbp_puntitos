package utec.apitester.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class JwtUtil {
    private static final String SECRET = "SuperSecreto-ChangeMe";

    public static String generateToken(String userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());

        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes());

        String signature = hmacSha256(header + "." + payload, SECRET);
        return header + "." + payload + "." + signature;
    }

    public static String validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String signatureCheck = hmacSha256(parts[0] + "." + parts[1], SECRET);
            if (!signatureCheck.equals(parts[2])) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            return payloadJson.replace("{\"sub\":\"", "").replace("\"}", "");
        } catch (Exception e) {
            return null;
        }
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}