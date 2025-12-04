package com.minibank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                       // generates getters, setters, toString, equals, hashCode
@NoArgsConstructor          // generates default constructor
@AllArgsConstructor         // generates constructor with all fields
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String tokenType = "Bearer";
    private Long expiresIn;
}
