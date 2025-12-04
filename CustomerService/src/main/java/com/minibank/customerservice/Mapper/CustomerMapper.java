package com.minibank.customerservice.Mapper;

import com.minibank.customerservice.DTOs.CustomerDTO;
import com.minibank.customerservice.Entity.Customer;

public class CustomerMapper {

    // Convert Entity -> DTO
    public static CustomerDTO toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setZip(customer.getZip());

        // Map only KYC ID (not full KYC object)
        dto.setKycId(customer.getKycId());

        return dto;
    }

    // Convert DTO -> Entity
    public static Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setZip(dto.getZip());

        // Set only KYC ID
        customer.setKycId(dto.getKycId());

        return customer;
    }
}
