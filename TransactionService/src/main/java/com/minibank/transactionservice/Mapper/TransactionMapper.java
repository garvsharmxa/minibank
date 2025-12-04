package com.minibank.transactionservice.Mapper;

import com.minibank.transactionservice.DTO.TransactionDto;
import com.minibank.transactionservice.Entity.Transaction;

public class TransactionMapper {

    // ENTITY → DTO
    public static TransactionDto toDto(Transaction t) {
        if (t == null) return null;

        return new TransactionDto(
                t.getId(),
                t.getCustomerId(),
                t.getAccountId(),
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

    // DTO → ENTITY
    public static Transaction toEntity(TransactionDto dto) {
        if (dto == null) return null;

        Transaction t = new Transaction();
        t.setId(dto.getId());
        t.setCustomerId(dto.getCustomerId());
        t.setAccountId(dto.getAccountId());
        t.setTransactionType(dto.getTransactionType());
        t.setTransactionMethod(dto.getTransactionMethod());
        t.setTransactionStatus(dto.getTransactionStatus());
        t.setAmount(dto.getAmount());
        t.setReferenceId(dto.getReferenceId());
        t.setOpeningBalance(dto.getOpeningBalance());
        t.setClosingBalance(dto.getClosingBalance());

        return t;
    }
}
