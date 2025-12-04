package com.minibank.customerservice. Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta. persistence.*;
import lombok.AllArgsConstructor;
import lombok. Data;
import lombok.NoArgsConstructor;
import org. hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;

    // Store only KYC ID, not Kyc entity
    @Column(name = "kyc_id")
    private UUID kycId;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @CreationTimestamp
    private Timestamp createdAt;
}
