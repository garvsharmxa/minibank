package com.minibank.customerservice.Repository;

import com.minibank.customerservice.Entity.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycRepository extends JpaRepository<Kyc, UUID> {
  boolean existsByPanNumber(String panNumber);

  boolean existsByAadharNumber(String aadharNumber);

  Optional<Kyc> findByPanNumber(String panNumber);
  Optional<Kyc> findByAadharNumber(String aadharNumber);

  Optional<Kyc> findByCustomerId(UUID customerId);  // <--- FIX THIS
}