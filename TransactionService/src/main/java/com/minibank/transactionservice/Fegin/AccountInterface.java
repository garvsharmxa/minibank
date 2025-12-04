package com.minibank.transactionservice.Fegin;

import com.minibank.transactionservice.ClientDto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "ACCOUNTSERVICE")
public interface AccountInterface {

    @GetMapping("/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable UUID accountId);

    @PostMapping("/accounts/{id}/deposit")
    AccountDTO deposit(@PathVariable UUID id, @RequestParam double amount);

    @PostMapping("/accounts/{id}/withdraw")
    AccountDTO withdraw(@PathVariable UUID id, @RequestParam double amount);

    @GetMapping("/accounts/{accountId}/exists")
    Boolean accountExists(@PathVariable UUID accountId);
}
