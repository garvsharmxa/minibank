package com.minibank.customerservice.Repository;

import com.minibank.customerservice.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  Optional<Customer> findByEmail(String email);
  Optional<Customer> findByPhone(String phone);
}