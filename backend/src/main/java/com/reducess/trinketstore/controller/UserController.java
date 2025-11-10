package com.reducess.trinketstore.controller;

import com.reducess.trinketstore.dto.UpdateUserRequest;
import com.reducess.trinketstore.dto.UserResponse;
import com.reducess.trinketstore.security.UserPrincipal;
import com.reducess.trinketstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de gerenciamento de usuários")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil atual", description = "Retorna os dados do usuário autenticado")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse response = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil", description = "Atualiza os dados do usuário autenticado")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Deletar própria conta", description = "Remove a conta do usuário autenticado")
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        userService.deleteUser(currentUser, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter usuário por ID (Admin)", description = "Retorna os dados de um usuário específico")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar usuário (Admin)", description = "Remove um usuário específico do sistema")
    public ResponseEntity<Void> deleteUserByAdmin(@PathVariable Long userId) {
        userService.deleteUserByAdmin(userId);
        return ResponseEntity.noContent().build();
    }
}
