package com.lutfudolay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutfudolay.service.IVagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vag")
@RequiredArgsConstructor
public class VagController {

	private final IVagService vagService;

    @PostMapping("/carplay")
    public ResponseEntity<String> enableCarplay() {
        return ResponseEntity.ok(vagService.enableCarplay());
    }

    @PostMapping("/video")
    public ResponseEntity<String> enableVideoInMotion() {
        return ResponseEntity.ok(vagService.enableVideoInMotion());
    }
}