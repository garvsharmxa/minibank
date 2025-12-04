package com.minibank.customerservice. Controller;

import com.minibank.customerservice.DTOs.CustomerDTO;
import com.minibank.customerservice.Entity.Customer;
import com.minibank.customerservice.Mapper.CustomerMapper;
import com.minibank.customerservice.Service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework. http.HttpStatus;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // CREATE CUSTOMER
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody CustomerDTO dto) {
        Customer saved = customerService.createCustomer(
                CustomerMapper.toEntity(dto)
        );
        return CustomerMapper.toDto(saved);
    }

    // GET ALL CUSTOMERS
    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomers()
                .stream()
                .map(CustomerMapper::toDto)
                .toList();
    }

    @PutMapping("/{customerId}/kyc/{kycId}")
    public ResponseEntity<String> updateCustomerKycId(
            @PathVariable UUID customerId,
            @PathVariable UUID kycId
    ) {
        customerService.updateKycId(customerId, kycId);
        return ResponseEntity.ok("KYC ID updated successfully");
    }

    // GET CUSTOMER BY ID
    @GetMapping("/{id}")
    public CustomerDTO getCustomerById(@PathVariable UUID id) {
        Customer customer = customerService.getCustomerById(id);
        return CustomerMapper.toDto(customer);
    }

    // CHECK IF CUSTOMER EXISTS (for inter-service communication)
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> customerExists(@PathVariable UUID id) {
        boolean exists = customerService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    // GET CUSTOMER DETAILS WITH KYC (for inter-service communication)
    @GetMapping("/{id}/details")
    public ResponseEntity<CustomerDTO> getCustomerDetails(@PathVariable UUID id) {
        Customer customer = customerService.getCustomerById(id);
        CustomerDTO dto = CustomerMapper.toDto(customer);
        return ResponseEntity.ok(dto);
    }

    // FULL UPDATE (PUT)
    @PutMapping("/{id}")
    public CustomerDTO updateCustomer(
            @PathVariable UUID id,
            @RequestBody CustomerDTO dto) {

        Customer updated = customerService.updateCustomer(
                id,
                CustomerMapper.toEntity(dto)
        );

        return CustomerMapper.toDto(updated);
    }

    // PARTIAL UPDATE (PATCH)
    @PatchMapping("/{id}")
    public CustomerDTO patchCustomer(
            @PathVariable UUID id,
            @RequestBody CustomerDTO dto) {

        Customer patched = customerService.patchCustomer(
                id,
                CustomerMapper.toEntity(dto)
        );

        return CustomerMapper.toDto(patched);
    }

    // DELETE CUSTOMER
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity. ok("Customer deleted successfully");
    }

    // GET CUSTOMER BY EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        Customer customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(CustomerMapper. toDto(customer));
    }

    // GET CUSTOMER BY PHONE
    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerDTO> getCustomerByPhone(@PathVariable String phone) {
        Customer customer = customerService. getCustomerByPhone(phone);
        return ResponseEntity.ok(CustomerMapper.toDto(customer));
    }
}