package com.minibank.cardservice.ClientDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private UUID id;
    private UUID customerId;
    private String accountNumber;

    private AccountType accountType;       // Local Enum
    private AccountStatus accountStatus;   // Local Enum

    private double accountBalance;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public enum AccountType {
        SAVING,
        CURRENT
    }

    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        BLOCKED
    }
}
