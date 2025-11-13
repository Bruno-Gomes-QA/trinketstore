package com.reducess.trinketstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reducess.trinketstore.config.SupabaseConfig;
import com.reducess.trinketstore.dto.CreateUserRequest;
import com.reducess.trinketstore.dto.UpdateUserByAdminRequest;
import com.reducess.trinketstore.dto.UpdateUserRequest;
import com.reducess.trinketstore.dto.UserResponse;
import com.reducess.trinketstore.entity.User;
import com.reducess.trinketstore.repository.UserRepository;
import com.reducess.trinketstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SupabaseConfig supabaseConfig;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserResponse getCurrentUser(UserPrincipal currentUser) {
        User user = userRepository.findByAuthId(currentUser.getAuthId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UserPrincipal currentUser, UpdateUserRequest request) {
        User user = userRepository.findByAuthId(currentUser.getAuthId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setNomeUser(request.getName());
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(UserPrincipal currentUser, String accessToken) {
        User user = userRepository.findByAuthId(currentUser.getAuthId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/admin/users/" + currentUser.getAuthId();

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .delete()
                    .addHeader("apikey", supabaseConfig.getServiceRoleKey())
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to delete user from Supabase");
                }
            }

            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/admin/users/" + user.getAuthId();

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .delete()
                    .addHeader("apikey", supabaseConfig.getServiceRoleKey())
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to delete user from Supabase");
                }
            }

            userRepository.delete(user);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Transactional
    public UserResponse updateUserByAdmin(Long userId, UpdateUserByAdminRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean needsSupabaseUpdate = false;

        // Update email in Supabase if provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            needsSupabaseUpdate = true;
            user.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setNomeUser(request.getName());
        }

        // Update role if provided
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole());
        }

        // Update email in Supabase Auth if needed
        if (needsSupabaseUpdate) {
            try {
                String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/admin/users/" + user.getAuthId();

                String jsonBody = objectMapper.writeValueAsString(
                        new UpdateUserSupabaseRequest(request.getEmail())
                );

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .put(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                        .addHeader("apikey", supabaseConfig.getServiceRoleKey())
                        .addHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        throw new RuntimeException("Failed to update user in Supabase: " + responseBody);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error updating user in Supabase: " + e.getMessage(), e);
            }
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        try {
            // Create user in Supabase Auth using admin endpoint
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/admin/users";

            String jsonBody = objectMapper.writeValueAsString(
                    new CreateUserSupabaseRequest(request.getEmail(), request.getPassword(), request.getEmail())
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("apikey", supabaseConfig.getServiceRoleKey())
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    throw new RuntimeException("Failed to create user in Supabase: " + responseBody);
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                UUID authId = UUID.fromString(jsonResponse.get("id").asText());

                // Create user in our database
                User user = new User();
                user.setAuthId(authId);
                user.setNomeUser(request.getName());
                user.setEmail(request.getEmail());
                user.setRole(request.getRole());
                userRepository.save(user);

                return mapToUserResponse(user);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getIdUser(),
                user.getAuthId(),
                user.getNomeUser(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private record CreateUserSupabaseRequest(String email, String password, String email_confirm) {}
    private record UpdateUserSupabaseRequest(String email) {}
}

