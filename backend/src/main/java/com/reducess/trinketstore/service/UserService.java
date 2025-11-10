package com.reducess.trinketstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reducess.trinketstore.config.SupabaseConfig;
import com.reducess.trinketstore.dto.UpdateUserRequest;
import com.reducess.trinketstore.dto.UserResponse;
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

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
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
}

