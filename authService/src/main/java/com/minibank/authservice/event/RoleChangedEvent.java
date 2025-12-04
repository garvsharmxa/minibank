package com.minibank.authservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangedEvent {
    private Long userId;
    private String username;
    private String email;
    private String action; // "ADDED" or "REMOVED"
    private String roleName;
}
