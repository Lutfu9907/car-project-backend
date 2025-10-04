package com.lutfudolay.license;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.Path;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LicenseService {

	@Value("${app.license.secret}")
    private String secret;

    @Value("${app.license.file}")
    private String licenseFilePath;

    @Value("${app.license.default-valid-days:365}")
    private int defaultValidDays;

    private final ObjectMapper mapper = new ObjectMapper();
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // Basit server-side doğrulama simülasyonu.
    // Gerçek ürün: server doğrulamalı. Burada "pattern" bazlı kontrol yapıyoruz.
    
    public boolean verifyLicenseKeyFormat(String licenseKey) {
        
        return licenseKey != null && licenseKey.matches("[A-Z0-9\\-]{8,40}");
    }

    public File createAndStoreLicense(LicenseRequest req) throws Exception {
        LocalDate validUntil = LocalDate.now().plusDays(defaultValidDays);

        LicensePayload payload = new LicensePayload();
        payload.setLicenseKey(req.getLicenseKey());
        payload.setOwner(req.getOwner() == null ? "UNKNOWN" : req.getOwner());
        payload.setHardwareId(req.getHardwareId() == null ? "UNKNOWN" : req.getHardwareId());
        payload.setValidUntil(validUntil.format(fmt));

        String json = mapper.writeValueAsString(payload);
        String sig = hmacSha256Base64(json, secret);

        LicenseFile file = new LicenseFile();
        file.setPayload(payload);
        file.setSignature(sig);

        // ensure dir exists
        File f = new File(licenseFilePath);
        f.getParentFile().mkdirs();

        mapper.writeValue(f, file);
        log.info("License written to {}", f.getAbsolutePath());
        return f;
    }

    public boolean validateLocalLicenseFile() {
        try {
            File f = new File(licenseFilePath);
            if (!f.exists()) return false;
            LicenseFile file = mapper.readValue(f, LicenseFile.class);
            String json = mapper.writeValueAsString(file.getPayload());
            String sig = hmacSha256Base64(json, secret);
            if (!sig.equals(file.getSignature())) {
                log.warn("License signature mismatch");
                return false;
            }
            // expiry check
            LocalDate until = LocalDate.parse(file.getPayload().getValidUntil(), fmt);
            return !LocalDate.now().isAfter(until);
        } catch (Exception e) {
            log.warn("Local license validate failed: {}", e.getMessage());
            return false;
        }
    }

    /* --- helpers --- */
    private String hmacSha256Base64(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(raw);
    }

    @Data
    public static class LicensePayload {
        private String licenseKey;
        private String owner;
        private String hardwareId;
        private String validUntil;
    }

    @Data
    public static class LicenseFile {
        private LicensePayload payload;
        private String signature;
    }
}
