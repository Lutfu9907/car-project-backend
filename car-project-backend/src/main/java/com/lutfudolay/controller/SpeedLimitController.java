package com.lutfudolay.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vag/speed-limit")
public class SpeedLimitController {

	@PostMapping("/remove")
    public Map<String, String> removeSpeedLimit() {
        System.out.println("🛠️ Speed limit removal command executed...");
        return Map.of("status", "success", "message", "Speed limit successfully removed.");
    }
}
