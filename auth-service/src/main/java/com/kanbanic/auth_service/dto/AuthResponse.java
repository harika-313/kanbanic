package com.kanbanic.auth_service.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
}