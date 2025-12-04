package com.minibank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                  
@NoArgsConstructor     
@AllArgsConstructor    
public class UserResponse {
    private Long id;
    private String username;
}
