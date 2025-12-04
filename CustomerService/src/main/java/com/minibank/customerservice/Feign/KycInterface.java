package com.minibank.customerservice.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "KYCSERVICE")
public interface KycInterface {

    @GetMapping("/kyc/{customerId}/verified")
    Boolean isKycVerified(@PathVariable UUID customerId);
}
