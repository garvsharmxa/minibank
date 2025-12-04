package com.minibank.authservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginEvent {
    private String username;
    private boolean success;
    private String ipAddress;
    private String details;
}
