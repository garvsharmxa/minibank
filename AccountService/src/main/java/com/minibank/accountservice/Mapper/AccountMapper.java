package com.minibank.accountservice.Mapper;

import com.minibank.accountservice.DTO.AccountDto;
import com.minibank.accountservice.Entity.Account;

public class AccountMapper {

    public static AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getAccountStatus(),
                account.getAccountBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
