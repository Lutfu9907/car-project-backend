package com.lutfudolay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutfudolay.service.impl.VagServiceMockImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vag")
@RequiredArgsConstructor
public class VagController {

	@Autowired
    private VagServiceMockImpl vagService;

    @PostMapping("/carplay")
    public ResponseEntity<String> enableCarPlay(@RequestBody(required = false) CarRequest request) {
        String vin = request != null ? request.getVin() : "UNKNOWN_VIN";
        return ResponseEntity.ok(vagService.enableCarplay(vin));
    }

    @PostMapping("/video")
    public ResponseEntity<String> enableVideo(@RequestBody(required = false) CarRequest request) {
        String vin = request != null ? request.getVin() : "UNKNOWN_VIN";
        return ResponseEntity.ok(vagService.enableVideoInMotion(vin));
    }

    // İç sınıf: basit DTO
    private static class CarRequest {
       private String vin;
       
       public String getVin() { 
        	return vin;
       }
       public void setVin(String vin) {
        	this.vin = vin;
        }
    }
}