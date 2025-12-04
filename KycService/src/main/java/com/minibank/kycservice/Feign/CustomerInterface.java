package com.minibank.kycservice.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(name = "CUSTOMERSERVICE")
public interface CustomerInterface {

    @PutMapping("/customers/{customerId}/kyc/{kycId}")
    void updateCustomerKycId(
            @PathVariable UUID customerId,
            @PathVariable UUID kycId
    );

    @GetMapping("/customers/{customerId}/exists")
    Boolean customerExists(@PathVariable UUID customerId);
}
