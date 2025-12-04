package com.minibank.kycservice.Mapper;

import com.minibank.kycservice.DTO.KycDTO;
import com.minibank.kycservice.Entity.Kyc;

public class KycMapper {

    // ENTITY → DTO
    public static KycDTO toDto(Kyc kyc) {
        if (kyc == null) return null;

        KycDTO dto = new KycDTO();
        dto.setId(kyc.getId());
        dto.setAadharNumber(kyc.getAadharNumber());
        dto.setPanNumber(kyc.getPanNumber());
        dto.setPanImageUrl(kyc.getPanImageUrl());
        dto.setAadharImageUrl(kyc.getAadharImageUrl());
        dto.setVerified(kyc.isVerified());
        dto.setCreatedOn(kyc.getCreatedOn());
        return dto;
    }

    // DTO → ENTITY
    public static Kyc toEntity(KycDTO dto){
        if (dto == null) return null;

        Kyc kyc = new Kyc();
        kyc.setId(dto.getId());
        kyc.setAadharNumber(dto.getAadharNumber());
        kyc.setPanNumber(dto.getPanNumber());
        kyc.setPanImageUrl(dto.getPanImageUrl());
        kyc.setAadharImageUrl(dto.getAadharImageUrl());
        kyc.setVerified(dto.isVerified());
        return kyc;
    }
}