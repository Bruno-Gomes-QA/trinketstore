package com.reducess.trinketstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reducess.trinketstore.config.SupabaseConfig;
import com.reducess.trinketstore.dto.*;
import com.reducess.trinketstore.entity.User;
import com.reducess.trinketstore.exception.AuthenticationException;
import com.reducess.trinketstore.exception.SignUpException;
import com.reducess.trinketstore.exception.UserNotFoundException;
import com.reducess.trinketstore.repository.UserRepository;
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
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    String errorMessage = parseSupabaseError(responseBody);
                    throw new SignUpException(errorMessage);
                }

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
        } catch (SignUpException e) {   
            throw e;
        } catch (Exception e) {
            throw new SignUpException("Erro ao criar conta. Por favor, tente novamente.", e);
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
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    String errorMessage = parseSupabaseError(responseBody);
                    throw new AuthenticationException(errorMessage);
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                UUID authId = UUID.fromString(jsonResponse.get("user").get("id").asText());
                String accessToken = jsonResponse.get("access_token").asText();
                String refreshToken = jsonResponse.get("refresh_token").asText();
                Long expiresIn = jsonResponse.get("expires_in").asLong();

                User user = userRepository.findByAuthId(authId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado no sistema"));

                UserResponse userResponse = mapToUserResponse(user);
                return new AuthResponse(accessToken, refreshToken, expiresIn, userResponse);
            }
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Erro ao fazer login. Por favor, tente novamente.", e);
        }
    }

    private String parseSupabaseError(String responseBody) {
        try {
            JsonNode errorJson = objectMapper.readTree(responseBody);

            if (errorJson.has("error_description")) {
                String errorDescription = errorJson.get("error_description").asText().toLowerCase();

                // Tradução de erros comuns do Supabase
                if (errorDescription.contains("invalid login credentials") ||
                    errorDescription.contains("invalid credentials")) {
                    return "Email ou senha inválidos";
                }

                if (errorDescription.contains("email not confirmed")) {
                    return "Email não confirmado. Por favor, verifique seu email";
                }

                if (errorDescription.contains("user already registered") ||
                    errorDescription.contains("already registered")) {
                    return "Este email já está cadastrado";
                }

                if (errorDescription.contains("password") && errorDescription.contains("weak")) {
                    return "A senha é muito fraca. Use uma senha mais forte";
                }

                if (errorDescription.contains("invalid email")) {
                    return "Email inválido";
                }

                if (errorDescription.contains("email rate limit exceeded")) {
                    return "Muitas tentativas. Por favor, aguarde alguns minutos";
                }
            }

            if (errorJson.has("message")) {
                return translateGenericError(errorJson.get("message").asText());
            }

            if (errorJson.has("msg")) {
                return translateGenericError(errorJson.get("msg").asText());
            }

        } catch (Exception e) {
            // Se não conseguir parsear, retorna mensagem genérica
        }

        return "Email ou senha inválidos";
    }

    private String translateGenericError(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("invalid") || lowerMessage.contains("wrong")) {
            return "Email ou senha inválidos";
        }

        if (lowerMessage.contains("not found")) {
            return "Usuário não encontrado";
        }

        if (lowerMessage.contains("already exists") || lowerMessage.contains("duplicate")) {
            return "Este email já está cadastrado";
        }

        return "Erro na autenticação. Por favor, tente novamente";
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
