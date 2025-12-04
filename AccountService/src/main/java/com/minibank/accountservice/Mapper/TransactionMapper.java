package com.minibank.accountservice.Mapper;

import com.minibank.accountservice.DTO.TransactionDto;
import com.minibank.accountservice.Entity.Transaction;

public class TransactionMapper {

    public static TransactionDto toDto(Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getCustomerId(),
                t.getAccount().getId(),
                t.getTransactionType(),
                t.getTransactionMethod(),
                t.getTransactionStatus(),
                t.getAmount(),
                t.getReferenceId(),
                t.getOpeningBalance(),
                t.getClosingBalance(),
                t.getTransactionDate()
        );
    }

    public static Transaction toEntity(TransactionDto dto) {
        Transaction t = new Transaction();
        t.setId(dto.getId());
        t.setCustomerId(dto.getCustomerId());
        t.setReferenceId(dto.getReferenceId());
        t.setAmount(dto.getAmount());
        t.setOpeningBalance(dto.getOpeningBalance());
        t.setClosingBalance(dto.getClosingBalance());
        t.setTransactionMethod(dto.getTransactionMethod());
        t.setTransactionType(dto.getTransactionType());
        t.setTransactionStatus(dto.getTransactionStatus());
        return t;
    }
}
