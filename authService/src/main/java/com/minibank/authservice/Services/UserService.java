package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.RefreshToken;
import com.minibank.authservice.Entity.Role;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.UserRepository;
import com.minibank.authservice.Utlity.JwtUtil;
import com.minibank.authservice.dto.AuthResponse;
import com.minibank.authservice.dto.LoginRequest;
import com.minibank.authservice.dto.RegisterRequest;
import com.minibank.authservice.dto.UserResponse;
import com.minibank.authservice.Event.LoginEvent;
import com.minibank.authservice.Event.UserCreatedEvent;
import com.minibank.authservice.exception.InvalidCredentialsException;
import com.minibank.authservice.exception.InvalidTokenException;
import com.minibank.authservice.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final long ACCESS_TOKEN_EXPIRY_MS = 1000 * 60 * 30; // 30 minutes


    public UserResponse register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
        }

        // Create new user
        Users user = new Users();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        
        // Assign default CUSTOMER role
        Role customerRole = roleService.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Default CUSTOMER role not found"));
        user.setRoles(new HashSet<>());
        user.getRoles().add(customerRole);
        
        Users savedUser = userRepository.save(user);
        
        // Publish user created event
        eventPublisher.publishEvent(new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        ));
        
        return new UserResponse(savedUser.getId(), savedUser.getUsername());
    }

    public AuthResponse login(LoginRequest loginRequest, String ipAddress) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Extract roles from authentication
                var roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                String accessToken = jwtUtil.generateToken(loginRequest.getUsername(), roles);
                
                // Create and save refresh token
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginRequest.getUsername());

                // Publish successful login event
                eventPublisher.publishEvent(new LoginEvent(
                        loginRequest.getUsername(),
                        true,
                        ipAddress,
                        "Successful login"
                ));

                return new AuthResponse(
                        accessToken,
                        refreshToken.getToken(),
                        loginRequest.getUsername(),
                        "Bearer",
                        ACCESS_TOKEN_EXPIRY_MS
                );
            }

            throw new InvalidCredentialsException("Authentication failed");
        } catch (Exception e) {
            // Publish failed login event
            eventPublisher.publishEvent(new LoginEvent(
                    loginRequest.getUsername(),
                    false,
                    ipAddress,
                    "Invalid credentials"
            ));
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public AuthResponse refreshAccessToken(String refreshTokenStr) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                    .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

            refreshToken = refreshTokenService.verifyExpiration(refreshToken);

            Users user = refreshToken.getUser();
            
            // Extract roles from user
            var roles = user.getRoles().stream()
                    .map(role -> "ROLE_" + role.getName())
                    .collect(Collectors.toList());

            String newAccessToken = jwtUtil.generateToken(user.getUsername(), roles);
            
            return new AuthResponse(
                    newAccessToken,
                    refreshTokenStr,
                    user.getUsername(),
                    "Bearer",
                    ACCESS_TOKEN_EXPIRY_MS
            );
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired refresh token: " + e.getMessage());
        }
    }

    /**
     * Logout user - revoke refresh token and clear security context
     */
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * Change user password
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenService.revokeUserTokens(user);

        // Publish password changed event
        eventPublisher.publishEvent(new com.minibank.authservice.Event.PasswordChangedEvent(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        ));
    }
}
