package com.minibank.accountservice.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class CustomerClient {

    private final WebClient webClient;

    public CustomerClient(@Value("${customer. service.url:http://localhost:8081}") String customerServiceUrl) {
        this. webClient = WebClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }

    public boolean customerExists(UUID customerId) {
        try {
            Boolean exists = webClient.get()
                    .uri("/api/customers/{customerId}/exists", customerId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return exists != null && exists;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify customer: " + e.getMessage());
        }
    }

    public CustomerDTO getCustomerDetails(UUID customerId) {
        return webClient.get()
                .uri("/api/customers/{customerId}", customerId)
                . retrieve()
                .bodyToMono(CustomerDTO.class)
                .block();
    }
}
