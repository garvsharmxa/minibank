package com.minibank.authservice.Controller;

import com.minibank.authservice.Services.UserManagementService;
import com.minibank.authservice.dto.ApiResponse;
import com.minibank.authservice.dto.AssignRoleRequest;
import com.minibank.authservice.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userManagementService.getAllUsers();
        
        ApiResponse<List<UserDto>> response = ApiResponse.success(
                users,
                "Users retrieved successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userManagementService.getUserById(id);
        
        ApiResponse<UserDto> response = ApiResponse.success(
                user,
                "User retrieved successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, 
                                                            @RequestBody UserDto userDto) {
        UserDto updatedUser = userManagementService.updateUser(id, userDto);
        
        ApiResponse<UserDto> response = ApiResponse.success(
                updatedUser,
                "User updated successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        
        ApiResponse<Object> response = ApiResponse.success(
                null,
                "User deleted successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> assignRole(@PathVariable Long id,
                                                            @Valid @RequestBody AssignRoleRequest request) {
        UserDto updatedUser = userManagementService.assignRole(id, request.getRoleName());
        
        ApiResponse<UserDto> response = ApiResponse.success(
                updatedUser,
                "Role assigned successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> removeRole(@PathVariable Long id,
                                                            @PathVariable String roleName) {
        UserDto updatedUser = userManagementService.removeRole(id, roleName);
        
        ApiResponse<UserDto> response = ApiResponse.success(
                updatedUser,
                "Role removed successfully",
                HttpStatus.OK.value()
        );
        
        return ResponseEntity.ok(response);
    }
}
