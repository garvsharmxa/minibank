package com.minibank.accountservice.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "KYCSERVICE")
public interface KycInterface {

    @GetMapping("/kyc/customer/{customerId}/exists")
    Boolean kycExists(@PathVariable UUID customerId);

    @GetMapping("/kyc/customer/{customerId}/verified")
    Boolean isKycVerified(@PathVariable UUID customerId);
}
