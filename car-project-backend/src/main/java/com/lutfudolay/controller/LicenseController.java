package com.lutfudolay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutfudolay.dto.LicenseRequest;
import com.lutfudolay.dto.LicenseResponse;
import com.lutfudolay.service.impl.LicenseService;

@RestController
@RequestMapping("/api/license")
public class LicenseController {

	@Autowired
    private LicenseService licenseService;

    @PostMapping("/verify")
    public LicenseResponse verify(@RequestBody LicenseRequest req) {
        try {
            if (req.getLicenseKey() == null || !licenseService.verifyLicenseKeyFormat(req.getLicenseKey())) {
                return new LicenseResponse(false, "License key format invalid");
            }

            // burada istersen remote sunucuya doğrulama isteği atabilirsin.
            // MVP: local format + create .lic file
            licenseService.createAndStoreLicense(req);
            return new LicenseResponse(true, "License stored locally");
        } catch (Exception e) {
            return new LicenseResponse(false, "Error: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public LicenseResponse status() {
        boolean ok = licenseService.validateLocalLicenseFile();
        return new LicenseResponse(ok, ok ? "License valid" : "License missing/invalid");
    }
}
