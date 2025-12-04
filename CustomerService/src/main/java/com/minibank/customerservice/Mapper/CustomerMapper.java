// CustomerService/src/main/java/com/minibank/customerservice/Mapper/CustomerMapper.java
package com.minibank.customerservice.Mapper;

import com.minibank.customerservice.DTOs.CustomerDTO;
import com.minibank.customerservice.DTOs.KycDTO;
import com.minibank.customerservice. Entity.Customer;
import com. minibank.customerservice.Entity. Kyc;

public class CustomerMapper {

    public static CustomerDTO toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto. setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto. setCity(customer.getCity());
        dto.setState(customer. getState());
        dto.setZip(customer.getZip());

        // Map KYC if present
        if (customer.getKyc() != null) {
            dto.setKyc(toKycDto(customer.getKyc()));
        }

        return dto;
    }

    private static KycDTO toKycDto(Kyc kyc) {
        if (kyc == null) {
            return null;
        }

        KycDTO dto = new KycDTO();
        dto.setId(kyc. getId());
        dto.setAadharNumber(kyc.getAadharNumber());
        dto.setPanNumber(kyc. getPanNumber());
        dto. setPanImageUrl(kyc. getPanImageUrl());
        dto.setAadharImageUrl(kyc.getAadharImageUrl());
        dto.setVerified(kyc.isVerified());
        dto.setCreatedOn(kyc.getCreatedOn());

        return dto;
    }

    public static Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer. setName(dto.getName());
        customer.setEmail(dto. getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto. getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setZip(dto.getZip());

        return customer;
    }
}