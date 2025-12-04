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

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Kyc kyc;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @CreationTimestamp
    private Timestamp createdAt;
}