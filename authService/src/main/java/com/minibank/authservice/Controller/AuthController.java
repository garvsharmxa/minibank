package com.minibank.authservice.Controller;

import com.minibank.authservice.Services.UserService;
import com.minibank.authservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = userService.register(registerRequest);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                userResponse,
                "User registered successfully",
                HttpStatus.CREATED.value()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userService.login(loginRequest);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                authResponse,
                "Login successful",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        AuthResponse authResponse = userService.refreshAccessToken(refreshTokenRequest.getRefreshToken());
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
                authResponse,
                "Token refreshed successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        userService.logout();
        
        ApiResponse<Object> response = ApiResponse.success(
                null,
                "Logged out successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }
}
