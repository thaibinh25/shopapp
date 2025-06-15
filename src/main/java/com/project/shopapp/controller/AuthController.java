package com.project.shopapp.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.project.shopapp.components.JwtTonkenUtil;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTonkenUtil jwtTokenUtil;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String code = body.get("token");

        try {
            // 1. Gửi mã code để lấy access_token
            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "code=" + code +
                                    "&client_id=" + clientId +
                                    "&client_secret=" + clientSecret +
                                    "&redirect_uri=" + redirectUri +
                                    "&grant_type=authorization_code"
                    ))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject tokenJson = new JSONObject(tokenResponse.body());
            if (!tokenJson.has("access_token")) {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "Google không trả về access_token. Response: " + tokenJson.toString()
                ));
            }

            String accessToken = tokenJson.getString("access_token");

            // 2. Lấy thông tin user từ access_token
            HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
            JSONObject userInfo = new JSONObject(userInfoResponse.body());

            String email = userInfo.getString("email");
            String sub = userInfo.getString("sub"); // Google ID
            String name = userInfo.optString("name", "No Name");

            // 3. Xử lý lưu user hoặc tìm user
            User user = userRepository.findByGoogleAccountId(sub)
                    .orElseGet(() -> {
                        Role defaultRole = roleRepository.findByName("user");
                        User newUser = User.builder()
                                .googleAccountId(sub)
                                .fullName(name)
                                .phoneNumber(null)
                                .active(true)
                                .password(null)
                                .role(defaultRole)
                                .build();
                        return userRepository.save(newUser);
                    });

            // 4. Tạo JWT và trả về
            String jwt = jwtTokenUtil.generateToken(user);
            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "message", "Login successful"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "OAuth2 login failed: " + e.getMessage()));
        }
    }


    // dùng cho Google Identity Services (One Tap hoặc Popup)

    /*@PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("token");

        try {
            // ✅ Thay bằng CLIENT_ID của bạn từ Google Cloud Console
            String CLIENT_ID = "582191794962-ksv61g67eppduhcmi5gp8s73v9chshm2.apps.googleusercontent.com";

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid Google ID Token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleAccountId = payload.getSubject(); // Unique ID từ Google
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 🔍 Tìm trong DB
            Optional<User> optionalUser = userRepository.findByGoogleAccountId(googleAccountId);
            User user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                Role defaultRole = roleRepository.findByName("user");
                user = User.builder()
                        .fullName(name)
                        .phoneNumber(null)
                        .googleAccountId(googleAccountId)
                        .password(null)
                        .active(true)
                        .role(defaultRole)
                        .build();
                userRepository.save(user);
            }

            // ✅ Tạo JWT
            String jwt = jwtTokenUtil.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "message", "Login successful"
            ));

        } catch (GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google token security issue: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }*/
}