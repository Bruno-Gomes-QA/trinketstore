package com.reducess.trinketstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long idUser;
    private UUID authId;
    private String nomeUser;
    private String role;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

