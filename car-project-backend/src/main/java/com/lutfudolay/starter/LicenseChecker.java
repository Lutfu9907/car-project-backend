package com.lutfudolay.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.lutfudolay.license.LicenseService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LicenseChecker implements CommandLineRunner{

	@Autowired
    private LicenseService licenseService;

    @Override
    public void run(String... args) {
        boolean ok = licenseService.validateLocalLicenseFile();
        if (!ok) {
            log.warn("License not found or invalid. Application will continue in limited/demo mode.");
            // isteğe bağlı: throw new IllegalStateException("License invalid"); -> uygulamayı durdur
        } else {
            log.info("License OK — application unlocked");
        }
    }
}
