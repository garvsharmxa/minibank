package com. minibank.accountservice.ClientDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql. Timestamp;
import java.util. UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycDTO {
    private UUID id;
    private String aadharNumber;
    private String panNumber;
    private String panImageUrl;
    private String aadharImageUrl;
    private boolean verified;
    private Timestamp createdOn;
}