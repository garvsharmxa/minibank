package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.Role;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.UserRepository;
import com.minibank.authservice.dto.UserDto;
import com.minibank.authservice.Event.RoleChangedEvent;
import com.minibank.authservice.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update basic fields
        if (userDto.getUsername() != null && !userDto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new UserAlreadyExistsException("Username already exists: " + userDto.getUsername());
            }
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getEnabled() != null) {
            user.setEnabled(userDto.getEnabled());
        }

        Users savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Prevent deletion of admin users (optional safety check)
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (isAdmin) {
            throw new AccessDeniedException("Cannot delete admin users");
        }
        
        userRepository.delete(user);
    }

    @Transactional
    public UserDto assignRole(UUID userId, String roleName) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);

            // Publish role changed event
            eventPublisher.publishEvent(new RoleChangedEvent(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    "ADDED",
                    roleName
            ));
        }

        return convertToDto(user);
    }

    @Transactional
    public UserDto removeRole(UUID userId, String roleName) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            userRepository.save(user);

            // Publish role changed event
            eventPublisher.publishEvent(new RoleChangedEvent(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    "REMOVED",
                    roleName
            ));
        }

        return convertToDto(user);
    }

    private UserDto convertToDto(Users user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.getEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
