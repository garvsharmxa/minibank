package com.minibank.customerservice.Service;

import com.minibank.customerservice.Config.AesEncryptor;
import com.minibank.customerservice.Entity.Customer;
import com.minibank.customerservice.Entity.Kyc;
import com.minibank.customerservice.Exceptions.KycException;
import com.minibank.customerservice.Repository.CustomerRepository;
import com.minibank.customerservice.Repository.KycRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KycService {

    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ------------------ CREATE ------------------
    @Autowired
    private S3Service s3Service;

    // ------------------ CREATE ------------------
    public Kyc createKyc(Kyc kyc, UUID customerId, byte[] panImage, byte[] aadharImage) {

        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new KycException("Customer not found with ID: " + customerId));

        // Encrypt PAN & Aadhar numbers
        String encryptedPan = AesEncryptor.encrypt(kyc.getPanNumber());
        String encryptedAadhar = AesEncryptor.encrypt(kyc.getAadharNumber());

        // Check duplicates
        if (kycRepository.existsByPanNumber(encryptedPan))
            throw new KycException("PAN already exists");

        if (kycRepository.existsByAadharNumber(encryptedAadhar))
            throw new KycException("Aadhar already exists");

        // Save encrypted data
        kyc.setPanNumber(encryptedPan);
        kyc.setAadharNumber(encryptedAadhar);

        // Upload Images (CloudFront URL returned automatically)
        if (panImage != null)
            kyc.setPanImageUrl(s3Service.uploadFile(panImage, "pan.jpg"));

        if (aadharImage != null)
            kyc.setAadharImageUrl(s3Service.uploadFile(aadharImage, "aadhar.jpg"));

        // Link customer BEFORE saving
        kyc.setCustomer(customer);

        // Save KYC
        Kyc savedKyc = kycRepository.save(kyc);

        // Attach to customer & save
        customer.setKyc(savedKyc);
        customerRepository.save(customer);

        // Return decrypted response
        return decryptResponse(savedKyc);
    }

    // ------------------ UPDATE ------------------
    public Kyc updateKyc(UUID id, Kyc updatedKyc, UUID customerId,
                         byte[] panImage, byte[] aadharImage) {

        Kyc existing = kycRepository.findById(id)
                .orElseThrow(() -> new KycException("KYC not found"));

        customerRepository.findById(customerId)
                .orElseThrow(() -> new KycException("Customer not found"));

        // PAN update
        if (updatedKyc.getPanNumber() != null) {
            String encPan = AesEncryptor.encrypt(updatedKyc.getPanNumber());
            if (kycRepository.existsByPanNumber(encPan))
                throw new KycException("PAN already exists");
            existing.setPanNumber(encPan);
        }

        // Aadhaar update
        if (updatedKyc.getAadharNumber() != null) {
            String encAd = AesEncryptor.encrypt(updatedKyc.getAadharNumber());
            if (kycRepository.existsByAadharNumber(encAd))
                throw new KycException("Aadhar already exists");
            existing.setAadharNumber(encAd);
        }

        // Upload new images
        if (panImage != null)
            existing.setPanImageUrl(s3Service.uploadFile(panImage, "pan.jpg"));

        if (aadharImage != null)
            existing.setAadharImageUrl(s3Service.uploadFile(aadharImage, "aadhar.jpg"));

        existing.setVerified(updatedKyc.isVerified());

        Kyc saved = kycRepository.save(existing);

        return decryptResponse(saved);
    }


    // ------------------ GET ALL ------------------
    public List<Kyc> getAllKyc() {
        List<Kyc> list = kycRepository.findAll();
        return list.stream().map(this::decryptResponse).toList();
    }

    public Kyc getKycByCustomerId(UUID customerId) {
        Kyc kyc = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new KycException("KYC not found with ID: " + customerId));
        return decryptResponse(kyc);
    }

    // ------------------ GET BY ID ------------------
    public Kyc getKycById(UUID id) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new KycException("KYC not found with ID: " + id));

        return decryptResponse(kyc);
    }

    // ------------------ DELETE ------------------
    public Kyc deleteKycById(UUID id) {
        Kyc kyc = getKycById(id);
        kycRepository.delete(kyc);
        return decryptResponse(kyc);
    }

    // ------------------ GET by PAN ------------------
    public Kyc getKycByPanNumber(String panNumber) {
        String encrypted = AesEncryptor.encrypt(panNumber);

        Kyc kyc = kycRepository.findByPanNumber(encrypted)
                .orElseThrow(() -> new KycException("KYC not found with PAN: " + panNumber));

        return decryptResponse(kyc);
    }

    // ------------------ GET by AADHAAR ------------------
    public Kyc getKycByAadhaarNumber(String aadhaarNumber) {
        String encrypted = AesEncryptor.encrypt(aadhaarNumber);

        Kyc kyc = kycRepository.findByAadharNumber(encrypted)
                .orElseThrow(() -> new KycException("KYC not found with Aadhaar: " + aadhaarNumber));

        return decryptResponse(kyc);
    }

    // ------------------ HELPER: DECRYPT API RESPONSE ------------------
    private Kyc decryptResponse(Kyc kyc) {
        kyc.setPanNumber(AesEncryptor.decrypt(kyc.getPanNumber()));
        kyc.setAadharNumber(AesEncryptor.decrypt(kyc.getAadharNumber()));
        return kyc;
    }
}
