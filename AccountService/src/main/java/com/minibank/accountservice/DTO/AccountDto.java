package com.minibank.accountservice.DTO;

import com.minibank.accountservice.Entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private UUID id;
    private UUID customerId;
    private String accountNumber;
    private Account.AccountType accountType;
    private Account.AccountStatus accountStatus;
    private double accountBalance;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
