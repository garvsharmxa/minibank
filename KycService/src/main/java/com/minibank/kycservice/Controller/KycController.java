package com.minibank.kycservice.Controller;

import com.minibank.kycservice.DTO.KycDTO;
import com.minibank.kycservice.Entity.Kyc;
import com.minibank.kycservice.Exception.KycException;
import com.minibank.kycservice.Mapper.KycMapper;
import com.minibank.kycservice.Service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    // CREATE KYC
    @PostMapping("/create")
    public ResponseEntity<KycDTO> createKyc(
            @RequestParam UUID customerId,
            @RequestParam String panNumber,
            @RequestParam String aadharNumber,
            @RequestParam(required = false) MultipartFile panImage,
            @RequestParam(required = false) MultipartFile aadharImage) throws IOException {

        // Check if KYC already exists for this customer
        try {
            kycService.getKycByCustomerId(customerId);
            // If we reach here, KYC already exists
            return ResponseEntity.status(409) // 409 Conflict
                    .body(null); // Or return error message
        } catch (KycException e) {
            // KYC doesn't exist, proceed with creation
        }

        Kyc kyc = new Kyc();
        kyc.setPanNumber(panNumber);
        kyc.setAadharNumber(aadharNumber);

        byte[] panBytes = (panImage != null && ! panImage.isEmpty())
                ? panImage.getBytes() : null;
        byte[] aadharBytes = (aadharImage != null && !aadharImage.isEmpty())
                ? aadharImage.getBytes() : null;

        Kyc created = kycService.createKyc(kyc, customerId, panBytes, aadharBytes);
        return ResponseEntity.ok(KycMapper.toDto(created));
    }

    // UPDATE KYC
    @PutMapping("/{id}")
    public ResponseEntity<Kyc> updateKyc(
            @PathVariable UUID id,
            @RequestParam UUID customerId,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String aadharNumber,
            @RequestParam(required = false, defaultValue = "false") boolean verified,
            @RequestParam(required = false) MultipartFile panImage,
            @RequestParam(required = false) MultipartFile aadharImage) throws IOException {

        Kyc updatedKyc = new Kyc();
        updatedKyc.setPanNumber(panNumber);
        updatedKyc.setAadharNumber(aadharNumber);
        updatedKyc.setVerified(verified);

        byte[] panBytes = (panImage != null && !panImage.isEmpty())
                ? panImage.getBytes()
                : null;

        byte[] aadharBytes = (aadharImage != null && !aadharImage.isEmpty())
                ? aadharImage.getBytes()
                : null;

        Kyc result = kycService.updateKyc(id, updatedKyc, customerId, panBytes, aadharBytes);
        return ResponseEntity.ok(result);
    }

    // GET ALL KYC
    @GetMapping
    public ResponseEntity<List<KycDTO>> getAllKyc() {
        return ResponseEntity.ok(
                kycService.getAllKyc().stream()
                        .map(KycMapper::toDto)
                        .toList()
        );
    }

    // GET KYC BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Kyc> getKycById(@PathVariable UUID id) {
        return ResponseEntity.ok(kycService.getKycById(id));
    }

    // GET KYC BY CUSTOMER ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Kyc> getKycByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(kycService.getKycByCustomerId(customerId));
    }

    // CHECK IF CUSTOMER KYC IS VERIFIED
    @GetMapping("/customer/{customerId}/verified")
    public Boolean isKycVerified(@PathVariable UUID customerId) {

        try {
            Kyc kyc = kycService.getKycByCustomerId(customerId);
            return kyc.isVerified();
        } catch (Exception e) {
            return false; // if KYC not found → return false
        }
    }


    // GET KYC BY PAN
    @GetMapping("/pan/{panNumber}")
    public ResponseEntity<Kyc> getKycByPan(@PathVariable String panNumber) {
        return ResponseEntity.ok(kycService.getKycByPanNumber(panNumber));
    }

    // GET KYC BY AADHAAR
    @GetMapping("/aadhaar/{aadhaarNumber}")
    public ResponseEntity<Kyc> getKycByAadhaar(@PathVariable String aadhaarNumber) {
        return ResponseEntity.ok(kycService.getKycByAadhaarNumber(aadhaarNumber));
    }

    // DELETE KYC
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteKyc(@PathVariable UUID id) {
        kycService.deleteKycById(id);
        return ResponseEntity.ok("KYC deleted successfully");
    }
}