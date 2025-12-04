package com.minibank.customerservice.Service;

import com.minibank.customerservice.Entity.Customer;
import com.minibank.customerservice.Exceptions.CustomerAlreadyExistsException;
import com.minibank.customerservice.Exceptions.CustomerNotFoundException;
import com.minibank.customerservice.Feign.KycInterface;
import com.minibank.customerservice.Repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;


    private KycInterface kycInterface; // Feign Client

    public CustomerService(KycInterface kycInterface) {

        this.kycInterface = kycInterface;
    }


    // ---------------- CREATE ----------------
    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating customer with email: {}", customer.getEmail());

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists: " + customer.getEmail());
        }

        if (customerRepository.existsByPhone(customer.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists: " + customer.getPhone());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", saved.getId());
        return saved;
    }


    // ---------------- READ ALL ----------------
    public List<Customer> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll();
    }


    // ---------------- READ ONE ----------------
    public Customer getCustomerById(UUID id) {
        log.info("Fetching customer with ID: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
    }


    // ---------------- CHECK IF EXISTS ----------------
    public boolean existsById(UUID id) {
        log.info("Checking if customer exists with ID: {}", id);
        return customerRepository.existsById(id);
    }


    // ---------------- GET BY EMAIL ----------------
    public Customer getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
    }


    // ---------------- GET BY PHONE ----------------
    public Customer getCustomerByPhone(String phone) {
        log.info("Fetching customer with phone: {}", phone);
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with phone: " + phone));
    }


    // ---------------- FULL UPDATE (PUT) ----------------
    @Transactional
    public Customer updateCustomer(UUID id, Customer updated) {
        log.info("Updating customer with ID: {}", id);

        Customer existing = getCustomerById(id);

        // Email uniqueness check
        if (updated.getEmail() != null &&
                !existing.getEmail().equals(updated.getEmail()) &&
                customerRepository.existsByEmail(updated.getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists: " + updated.getEmail());
        }

        // Phone uniqueness check
        if (updated.getPhone() != null &&
                !existing.getPhone().equals(updated.getPhone()) &&
                customerRepository.existsByPhone(updated.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists: " + updated.getPhone());
        }

        // Update fields
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setZip(updated.getZip());

        // Update KYC ID only if provided
        if (updated.getKycId() != null) {
            existing.setKycId(updated.getKycId());
        }

        Customer saved = customerRepository.save(existing);
        log.info("Customer updated successfully with ID: {}", saved.getId());

        return saved;
    }


    // ---------------- PARTIAL UPDATE (PATCH) ----------------
    @Transactional
    public Customer patchCustomer(UUID id, Customer partial) {
        log.info("Patching customer with ID: {}", id);

        Customer existing = getCustomerById(id);

        // Email uniqueness
        if (partial.getEmail() != null &&
                !existing.getEmail().equals(partial.getEmail()) &&
                customerRepository.existsByEmail(partial.getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists: " + partial.getEmail());
        }

        // Phone uniqueness
        if (partial.getPhone() != null &&
                !existing.getPhone().equals(partial.getPhone()) &&
                customerRepository.existsByPhone(partial.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists: " + partial.getPhone());
        }

        // Update only provided fields
        if (partial.getName() != null) existing.setName(partial.getName());
        if (partial.getEmail() != null) existing.setEmail(partial.getEmail());
        if (partial.getPhone() != null) existing.setPhone(partial.getPhone());
        if (partial.getAddress() != null) existing.setAddress(partial.getAddress());
        if (partial.getCity() != null) existing.setCity(partial.getCity());
        if (partial.getState() != null) existing.setState(partial.getState());
        if (partial.getZip() != null) existing.setZip(partial.getZip());
        if (partial.getKycId() != null) existing.setKycId(partial.getKycId());

        Customer saved = customerRepository.save(existing);
        log.info("Customer patched successfully with ID: {}", saved.getId());

        return saved;
    }


    // ---------------- DELETE ----------------
    @Transactional
    public void deleteCustomer(UUID id) {
        log.warn("Deleting customer with ID: {}", id);

        Customer customer = getCustomerById(id);

        log.warn("Removing customer {}", id);
        customerRepository.delete(customer);
    }


    @Transactional
    public void updateKycId(UUID customerId, UUID kycId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setKycId(kycId);
        customerRepository.save(customer);
    }


    // ---------------- VALIDATE CUSTOMER FOR ACCOUNT CREATION ----------------
    public boolean canCreateAccount(UUID customerId) {
        Customer customer = getCustomerById(customerId);

        if (customer.getKycId() == null) {
            log.warn("Customer {} does not have KYC ID", customerId);
            return false;
        }

        // Call KYC service to check verification
        Boolean isVerified = kycInterface.isKycVerified(customer.getKycId());

        if (Boolean.FALSE.equals(isVerified)) {
            log.warn("Customer {} KYC is not verified", customerId);
            return false;
        }

        return true;
    }
}
