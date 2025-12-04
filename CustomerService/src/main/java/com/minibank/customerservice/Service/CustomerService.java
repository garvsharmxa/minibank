package com.minibank.customerservice.Service;

import com.minibank.customerservice.Config.AesEncryptor;
import com.minibank. customerservice.Entity.Customer;
import com.minibank.customerservice.Entity.Kyc;
import com.minibank.customerservice.Exceptions.CustomerAlreadyExistsException;
import com.minibank.customerservice. Exceptions.CustomerNotFoundException;
import com.minibank.customerservice.Repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework. beans.factory.annotation.Autowired;
import org.springframework. stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

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
        List<Customer> list = customerRepository. findAll();
        return list. stream().map(this::decryptCustomerKyc).toList();
    }

    // ---------------- READ ONE ----------------
    public Customer getCustomerById(UUID id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        return decryptCustomerKyc(customer);
    }

    // ---------------- CHECK IF EXISTS ----------------
    public boolean existsById(UUID id) {
        log.info("Checking if customer exists with ID: {}", id);
        return customerRepository.existsById(id);
    }

    // ---------------- GET BY EMAIL ----------------
    public Customer getCustomerByEmail(String email) {
        log.info("Fetching customer with email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));

        return decryptCustomerKyc(customer);
    }

    // ---------------- GET BY PHONE ----------------
    public Customer getCustomerByPhone(String phone) {
        log.info("Fetching customer with phone: {}", phone);
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with phone: " + phone));

        return decryptCustomerKyc(customer);
    }

    // ---------------- FULL UPDATE (PUT) ----------------
    @Transactional
    public Customer updateCustomer(UUID id, Customer updated) {
        log. info("Updating customer with ID: {}", id);

        Customer existing = getCustomerById(id);

        // Check email uniqueness if changed
        if (updated.getEmail() != null &&
                ! existing.getEmail().equals(updated.getEmail()) &&
                customerRepository.existsByEmail(updated. getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists: " + updated.getEmail());
        }

        // Check phone uniqueness if changed
        if (updated.getPhone() != null &&
                !existing.getPhone(). equals(updated.getPhone()) &&
                customerRepository.existsByPhone(updated.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists: " + updated.getPhone());
        }

        // Update all fields
        existing.setName(updated.getName());
        existing. setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing. setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing. setState(updated.getState());
        existing.setZip(updated.getZip());

        // Don't update KYC here - use KycService for that
        if (updated.getKyc() != null) {
            existing.setKyc(updated. getKyc());
        }

        Customer saved = customerRepository. save(existing);
        log. info("Customer updated successfully with ID: {}", saved.getId());

        return decryptCustomerKyc(saved);
    }

    // ---------------- PARTIAL UPDATE (PATCH) ----------------
    @Transactional
    public Customer patchCustomer(UUID id, Customer partial) {
        log.info("Patching customer with ID: {}", id);

        Customer existing = getCustomerById(id);

        // Check email uniqueness if provided and changed
        if (partial.getEmail() != null &&
                !existing.getEmail().equals(partial. getEmail()) &&
                customerRepository.existsByEmail(partial.getEmail())) {
            throw new CustomerAlreadyExistsException("Email already exists: " + partial.getEmail());
        }

        // Check phone uniqueness if provided and changed
        if (partial. getPhone() != null &&
                !existing.getPhone().equals(partial.getPhone()) &&
                customerRepository.existsByPhone(partial.getPhone())) {
            throw new CustomerAlreadyExistsException("Phone already exists: " + partial.getPhone());
        }

        // Update only provided fields
        if (partial.getName() != null) {
            existing.setName(partial.getName());
        }
        if (partial.getEmail() != null) {
            existing.setEmail(partial.getEmail());
        }
        if (partial. getPhone() != null) {
            existing.setPhone(partial. getPhone());
        }
        if (partial.getAddress() != null) {
            existing.setAddress(partial.getAddress());
        }
        if (partial. getCity() != null) {
            existing.setCity(partial. getCity());
        }
        if (partial.getState() != null) {
            existing.setState(partial.getState());
        }
        if (partial.getZip() != null) {
            existing.setZip(partial.getZip());
        }
        if (partial.getKyc() != null) {
            existing.setKyc(partial.getKyc());
        }

        Customer saved = customerRepository.save(existing);
        log.info("Customer patched successfully with ID: {}", saved.getId());

        return decryptCustomerKyc(saved);
    }

    // ---------------- DELETE ----------------
    @Transactional
    public void deleteCustomer(UUID id) {
        log.warn("Deleting customer with ID: {}", id);

        Customer customer = getCustomerById(id);

        // Optional: Check if customer has related data
        if (customer.getKyc() != null) {
            log.warn("Deleting customer with existing KYC data");
        }

        customerRepository. delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    // ---------------- DECRYPT CUSTOMER'S KYC FIELDS ----------------
    private Customer decryptCustomerKyc(Customer customer) {
        if (customer != null && customer.getKyc() != null) {
            Kyc kyc = customer.getKyc();

            try {
                if (kyc.getPanNumber() != null && ! kyc.getPanNumber().isEmpty()) {
                    kyc. setPanNumber(AesEncryptor.decrypt(kyc.getPanNumber()));
                }
            } catch (Exception e) {
                log.error("Error decrypting PAN number for customer: {}", customer.getId(), e);
                kyc.setPanNumber("ERROR_DECRYPTING");
            }

            try {
                if (kyc.getAadharNumber() != null && ! kyc.getAadharNumber().isEmpty()) {
                    kyc.setAadharNumber(AesEncryptor.decrypt(kyc.getAadharNumber()));
                }
            } catch (Exception e) {
                log.error("Error decrypting Aadhar number for customer: {}", customer.getId(), e);
                kyc.setAadharNumber("ERROR_DECRYPTING");
            }
        }
        return customer;
    }

    // ---------------- VALIDATE CUSTOMER FOR ACCOUNT CREATION ----------------
    public boolean canCreateAccount(UUID customerId) {
        Customer customer = getCustomerById(customerId);

        // Check if customer has KYC and it's verified
        if (customer.getKyc() == null) {
            log.warn("Customer {} does not have KYC information", customerId);
            return false;
        }

        if (!customer.getKyc(). isVerified()) {
            log.warn("Customer {} KYC is not verified", customerId);
            return false;
        }

        return true;
    }
}