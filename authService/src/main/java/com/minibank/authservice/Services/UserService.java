package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.UserRepository;
import com.minibank.authservice.Utlity.JwtUtil;
import com.minibank.authservice.dto.AuthResponse;
import com.minibank.authservice.dto.LoginRequest;
import com.minibank.authservice.dto.RegisterRequest;
import com.minibank.authservice.dto.UserResponse;
import com.minibank.authservice.exception.InvalidCredentialsException;
import com.minibank.authservice.exception.InvalidTokenException;
import com.minibank.authservice.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final long ACCESS_TOKEN_EXPIRY_MS = 1000 * 60 * 60; // 1 hour

    /**
     * Register a new user
     * @param registerRequest User registration details
     * @return UserResponse with user information
     * @throws UserAlreadyExistsException if username already exists
     */
    public UserResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        Users existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }

        // Create new user
        Users user = new Users();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        Users savedUser = userRepository.save(user);
        
        return new UserResponse(savedUser.getId(), savedUser.getUsername());
    }

    /**
     * Authenticate user and generate tokens
     * @param loginRequest User login credentials
     * @return AuthResponse with access and refresh tokens
     * @throws InvalidCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);

                String accessToken = jwtUtil.generateToken(loginRequest.getUsername());
                String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

                return new AuthResponse(
                        accessToken,
                        refreshToken,
                        loginRequest.getUsername(),
                        "Bearer",
                        ACCESS_TOKEN_EXPIRY_MS
                );
            }

            throw new InvalidCredentialsException("Authentication failed");
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    /**
     * Refresh access token using refresh token
     * @param refreshToken Refresh token
     * @return AuthResponse with new access token
     * @throws InvalidTokenException if refresh token is invalid
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            
            if (jwtUtil.validateRefreshToken(refreshToken, username)) {
                String newAccessToken = jwtUtil.generateToken(username);
                
                return new AuthResponse(
                        newAccessToken,
                        refreshToken,
                        username,
                        "Bearer",
                        ACCESS_TOKEN_EXPIRY_MS
                );
            }
            
            throw new InvalidTokenException("Invalid refresh token");
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
    }

    /**
     * Logout user - clear security context
     * Note: In a stateless JWT setup, actual token invalidation would require
     * a token blacklist or database tracking which is beyond basic implementation.
     * For now, we clear the security context.
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
