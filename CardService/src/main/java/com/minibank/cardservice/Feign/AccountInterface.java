package com.minibank.cardservice.Feign;


import com.minibank.cardservice.ClientDto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ACCOUNTSERVICE")
public interface AccountInterface {

    @GetMapping("/accounts/{accountId}")
    AccountDTO getAccountById(@PathVariable UUID accountId);

    @GetMapping("/accounts/customer/{customerId}")
    AccountDTO getAccountByCustomerId(@PathVariable UUID customerId);

    @GetMapping("/accounts/{accountId}/exists")
    Boolean accountExists(@PathVariable UUID accountId);
}
