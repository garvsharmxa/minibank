package com.minibank.accountservice.Feign;

import com.minibank.accountservice.ClientDto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "CUSTOMERSERVICE")
public interface CustomerInterface {

    @GetMapping("/customers/{customerId}/exists")
    Boolean customerExists(@PathVariable UUID customerId);

    @GetMapping("/customers/{customerId}")
    CustomerDTO getCustomerDetails(@PathVariable UUID customerId);
}
