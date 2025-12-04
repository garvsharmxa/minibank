package com.minibank.authservice.Controller;

import com.minibank.authservice.Services.UserService;
import com.minibank.authservice.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = userService.register(registerRequest);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                userResponse,
                "User registered successfully",
                HttpStatus.CREATED.value()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest, 
                                                           HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        AuthResponse authResponse = userService.login(loginRequest, ipAddress);
        
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
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody(required = false) RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null;
        userService.logout(refreshToken);
        
        ApiResponse<Object> response = ApiResponse.success(
                null,
                "Logged out successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        
        userService.changePassword(
                userDetails.getUsername(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        
        ApiResponse<Object> response = ApiResponse.success(
                null,
                "Password changed successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address from request, handling proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
