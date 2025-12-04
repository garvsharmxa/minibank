package com.minibank.customerservice.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Kyc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String aadharNumber;
    private String panNumber;
    private String panImageUrl;
    private String aadharImageUrl;

    @Column(name = "is_verified")
    private boolean verified;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    @JsonBackReference
    private Customer customer;

    @CreationTimestamp
    private Timestamp createdOn;
}