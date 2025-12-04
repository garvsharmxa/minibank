package com.minibank.authservice.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangedEvent {
    private UUID userId;
    private String username;
    private String email;
    private String action; // "ADDED" or "REMOVED"
    private String roleName;
}
