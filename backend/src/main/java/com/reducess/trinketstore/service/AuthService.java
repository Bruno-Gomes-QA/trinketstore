package com.reducess.trinketstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reducess.trinketstore.config.SupabaseConfig;
import com.reducess.trinketstore.dto.*;
import com.reducess.trinketstore.entity.User;
import com.reducess.trinketstore.repository.UserRepository;
import com.reducess.trinketstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SupabaseConfig supabaseConfig;
    private final UserRepository userRepository;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/signup";

            String jsonBody = objectMapper.writeValueAsString(
                    new SignUpSupabaseRequest(request.getEmail(), request.getPassword())
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("apikey", supabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to create user in Supabase: " + response.body().string());
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                UUID authId = UUID.fromString(jsonResponse.get("user").get("id").asText());
                String accessToken = jsonResponse.get("access_token").asText();
                String refreshToken = jsonResponse.get("refresh_token").asText();
                Long expiresIn = jsonResponse.get("expires_in").asLong();

                User user = new User();
                user.setAuthId(authId);
                user.setNomeUser(request.getName());
                user.setRole("customer");
                userRepository.save(user);

                UserResponse userResponse = mapToUserResponse(user);
                return new AuthResponse(accessToken, refreshToken, expiresIn, userResponse);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during sign up: " + e.getMessage(), e);
        }
    }

    public AuthResponse signIn(SignInRequest request) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";

            String jsonBody = objectMapper.writeValueAsString(
                    new SignInSupabaseRequest(request.getEmail(), request.getPassword())
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("apikey", supabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Invalid credentials");
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                UUID authId = UUID.fromString(jsonResponse.get("user").get("id").asText());
                String accessToken = jsonResponse.get("access_token").asText();
                String refreshToken = jsonResponse.get("refresh_token").asText();
                Long expiresIn = jsonResponse.get("expires_in").asLong();

                User user = userRepository.findByAuthId(authId)
                        .orElseThrow(() -> new RuntimeException("User not found in database"));

                UserResponse userResponse = mapToUserResponse(user);
                return new AuthResponse(accessToken, refreshToken, expiresIn, userResponse);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during sign in: " + e.getMessage(), e);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getIdUser(),
                user.getAuthId(),
                user.getNomeUser(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private record SignUpSupabaseRequest(String email, String password) {}
    private record SignInSupabaseRequest(String email, String password) {}
}

