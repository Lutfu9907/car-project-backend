package com.lutfudolay.license;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/license")
@CrossOrigin(origins = "*")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    /**
     * Kullanıcı lisans anahtarını girdiğinde doğrulayıp .lic dosyasına kaydeder.
     */
    @PostMapping("/activate")
    public LicenseResponse activateLicense(@RequestBody LicenseRequest req) {
        try {
            if (req.getLicenseKey() == null || req.getLicenseKey().isBlank()) {
                return new LicenseResponse(false, "Lisans anahtarı boş olamaz.");
            }

            // Lisans format kontrolü
            if (!licenseService.verifyLicenseKeyFormat(req.getLicenseKey())) {
                return new LicenseResponse(false, "Geçersiz lisans formatı.");
            }

            // Lisansı oluştur ve kaydet
            licenseService.createAndStoreLicense(req);
            return new LicenseResponse(true, "Lisans başarıyla etkinleştirildi.");
        } catch (Exception e) {
            return new LicenseResponse(false, "Hata: " + e.getMessage());
        }
    }

    /**
     * Uygulama açıldığında lisans durumu sorgulama (örn: FULL veya DEMO modu)
     */
    @GetMapping("/status")
    public LicenseResponse getLicenseStatus() {
        boolean valid = licenseService.validateLocalLicenseFile();
        return new LicenseResponse(valid,
                valid ? "Lisans geçerli. FULL mod aktif." : "Lisans geçersiz veya bulunamadı. DEMO mod aktif.");
    }
    
    @GetMapping("/details")
    public LicenseService.LicensePayload getLicenseDetails() {
        return licenseService.getLicenseDetails();
    }
}
