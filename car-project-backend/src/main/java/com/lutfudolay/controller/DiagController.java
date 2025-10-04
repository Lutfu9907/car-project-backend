package com.lutfudolay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutfudolay.service.impl.ElmServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/obd")
@RequiredArgsConstructor
public class DiagController {

	private final ElmServiceImpl elm;

    @GetMapping("/ports")
    public ResponseEntity<?> ports() {
        return ResponseEntity.ok(elm.listPorts());
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect() {
        elm.connect();
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/rpm")
    public ResponseEntity<?> rpm() {
        int rpm = elm.readRpm();
        return ResponseEntity.ok(rpm);
    }
    
    @GetMapping("/vin")
    public ResponseEntity<String> readVin() {
        String vin = elm.readVin();
        return ResponseEntity.ok(vin);
    }
}
