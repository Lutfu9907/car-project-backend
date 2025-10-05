package com.lutfudolay.license;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    // 1️⃣ Lisans formatını basitçe doğrula (örnek: ABCD-1234-EFGH)
    public boolean verifyLicenseKeyFormat(String licenseKey) {
        return licenseKey != null && licenseKey.matches("[A-Z0-9\\-]{10,}");
    }

    // 2️⃣ Lisans dosyası oluştur ve kaydet
    public Path createAndStoreLicense(LicenseRequest req) throws Exception {
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

        File f = new File(licenseFilePath);
        if (f.getParentFile() != null)
            f.getParentFile().mkdirs();

        mapper.writeValue(f, file);
        log.info("License written to {}", f.getAbsolutePath());
        return f.toPath();
    }

    // 3️⃣ Yerel lisans dosyasını kontrol et
    public boolean validateLocalLicenseFile() {
        try {
            File f = new File(licenseFilePath);
            if (!f.exists()) {
                log.warn("License file not found: {}", f.getAbsolutePath());
                return false;
            }

            LicenseFile file = mapper.readValue(f, LicenseFile.class);
            String json = mapper.writeValueAsString(file.getPayload());
            String sig = hmacSha256Base64(json, secret);

            if (!sig.equals(file.getSignature())) {
                log.warn("License signature mismatch");
                return false;
            }

            LocalDate until = LocalDate.parse(file.getPayload().getValidUntil(), fmt);
            if (LocalDate.now().isAfter(until)) {
                log.warn("License expired on {}", until);
                return false;
            }

            log.info("License valid until {}", until);
            return true;
        } catch (Exception e) {
            log.warn("Local license validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    public LicensePayload getLicenseDetails() {
        try {
            File f = new File(licenseFilePath);
            if (!f.exists()) {
                log.warn("Lisans dosyası bulunamadı: {}", licenseFilePath);
                return null;
            }

            LicenseFile file = mapper.readValue(f, LicenseFile.class);
            return file.getPayload();
        } catch (Exception e) {
            log.error("Lisans bilgileri okunamadı: {}", e.getMessage());
            return null;
        }
    }


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
