package com.minibank.kycservice.Service;

import com.minibank.kycservice.Config.AesEncryptor;
import com.minibank.kycservice.Entity.Kyc;
import com.minibank.kycservice.Exception.KycException;
import com.minibank.kycservice.Feign.CustomerInterface;
import com.minibank.kycservice.Repository.KycRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KycService {

    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private CustomerInterface customerInterface;

    @Autowired
    private S3Service s3Service;


    // ------------------ CREATE KYC ------------------
    public Kyc createKyc(Kyc kyc, UUID customerId, byte[] panImage, byte[] aadharImage) {

        // 1️⃣ Validate Customer using Feign
        Boolean exists = customerInterface.customerExists(customerId);
        if (exists == null || !exists) {
            throw new KycException("Customer not found with ID: " + customerId);
        }

        // 2️⃣ Encrypt PAN & Aadhaar
        String encryptedPan = AesEncryptor.encrypt(kyc.getPanNumber());
        String encryptedAadhar = AesEncryptor.encrypt(kyc.getAadharNumber());

        if (kycRepository.existsByPanNumber(encryptedPan)) {
            throw new KycException("PAN already exists");
        }

        if (kycRepository.existsByAadharNumber(encryptedAadhar)) {
            throw new KycException("Aadhar already exists");
        }

        kyc.setPanNumber(encryptedPan);
        kyc.setAadharNumber(encryptedAadhar);

        // 3️⃣ Upload Images to S3
        if (panImage != null) {
            kyc.setPanImageUrl(s3Service.uploadFile(panImage, "pan.jpg"));
        }

        if (aadharImage != null) {
            kyc.setAadharImageUrl(s3Service.uploadFile(aadharImage, "aadhar.jpg"));
        }

        // 4️⃣ Save Customer ID inside KYC
        kyc.setCustomerId(customerId);

        // 5️⃣ Save KYC into DB
        Kyc saved = kycRepository.save(kyc);

        // 6️⃣ Update Customer DB with NEW KYC ID
        customerInterface.updateCustomerKycId(customerId, saved.getId());

        // 7️⃣ Return decrypted KYC
        return decryptResponse(saved);
    }



    // ------------------ UPDATE KYC ------------------
    public Kyc updateKyc(UUID id, Kyc updatedKyc, UUID customerId,
                         byte[] panImage, byte[] aadharImage) {

        Kyc existing = kycRepository.findById(id)
                .orElseThrow(() -> new KycException("KYC not found"));

        // Validate customer exists
        Boolean exists = customerInterface.customerExists(customerId);
        if (exists == null || !exists) {
            throw new KycException("Customer not found");
        }

        // PAN update
        if (updatedKyc.getPanNumber() != null) {
            String encPan = AesEncryptor.encrypt(updatedKyc.getPanNumber());
            if (kycRepository.existsByPanNumber(encPan)) {
                throw new KycException("PAN already exists");
            }
            existing.setPanNumber(encPan);
        }

        // Aadhaar update
        if (updatedKyc.getAadharNumber() != null) {
            String encAd = AesEncryptor.encrypt(updatedKyc.getAadharNumber());
            if (kycRepository.existsByAadharNumber(encAd)) {
                throw new KycException("Aadhar already exists");
            }
            existing.setAadharNumber(encAd);
        }

        // Image updates
        if (panImage != null) {
            existing.setPanImageUrl(s3Service.uploadFile(panImage, "pan.jpg"));
        }

        if (aadharImage != null) {
            existing.setAadharImageUrl(s3Service.uploadFile(aadharImage, "aadhar.jpg"));
        }

        existing.setVerified(updatedKyc.isVerified());

        return decryptResponse(kycRepository.save(existing));
    }



    // ------------------ GET METHODS ------------------
    public List<Kyc> getAllKyc() {
        return kycRepository.findAll()
                .stream()
                .map(this::decryptResponse)
                .toList();
    }

    public Kyc getKycByCustomerId(UUID customerId) {
        Kyc kyc = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new KycException("KYC not found for customer " + customerId));
        return decryptResponse(kyc);
    }

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


    // ------------------ SEARCH PAN / AADHAAR ------------------
    public Kyc getKycByPanNumber(String panNumber) {
        String encrypted = AesEncryptor.encrypt(panNumber);

        Kyc kyc = kycRepository.findByPanNumber(encrypted)
                .orElseThrow(() -> new KycException("KYC not found for PAN: " + panNumber));

        return decryptResponse(kyc);
    }

    public Kyc getKycByAadhaarNumber(String aadhaarNumber) {
        String encrypted = AesEncryptor.encrypt(aadhaarNumber);

        Kyc kyc = kycRepository.findByAadharNumber(encrypted)
                .orElseThrow(() -> new KycException("KYC not found for Aadhaar: " + aadhaarNumber));

        return decryptResponse(kyc);
    }


    // ------------------ DECRYPT RESPONSE ------------------
    private Kyc decryptResponse(Kyc kyc) {
        kyc.setPanNumber(AesEncryptor.decrypt(kyc.getPanNumber()));
        kyc.setAadharNumber(AesEncryptor.decrypt(kyc.getAadharNumber()));
        return kyc;
    }
}
