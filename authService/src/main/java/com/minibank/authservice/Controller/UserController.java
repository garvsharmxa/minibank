package com.minibank.authservice.Controller;

import com.minibank.authservice.Services.UserService;
import com.minibank.authservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = userService.register(registerRequest);

        ApiResponse<UserResponse> response = ApiResponse.success(
                userResponse,
                "User registered successfully",
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = userService.login(loginRequest);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                authResponse,
                "Login successful",
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(response);
    }
}
