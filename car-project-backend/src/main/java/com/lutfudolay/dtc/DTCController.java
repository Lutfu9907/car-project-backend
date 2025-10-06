package com.lutfudolay.dtc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dtc")
@CrossOrigin(origins = "*")
public class DTCController {

	@Autowired
    private DTCService dtcService;

    @GetMapping("/read")
    public List<Map<String, String>> readCodes() {
        return dtcService.readDTCs();
    }

    @PostMapping("/clear")
    public String clearCodes() {
        return dtcService.clearDTCs();
    }
}
