package com.lutfudolay.dtc;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lutfudolay.service.impl.ElmServiceImpl;
import com.lutfudolay.service.impl.OperationHistoryService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DTCService {

	  @Autowired
	    private ElmServiceImpl elm;

	    @Autowired
	    private OperationHistoryService history;

	    /**
	     * Mode 03 - Read Stored DTCs
	     */
	    public List<Map<String, String>> readDTCs() {
	        List<Map<String, String>> result = new ArrayList<>();
	        try {
	            String resp = elm.send("03"); // Mode 03 → Stored DTCs

	            if (resp == null || resp.isBlank()) {
	                log.warn("Empty DTC response");
	                return result;
	            }

	            // Mock veri veya gerçek yanıt
	            if (resp.contains("NO")) {
	                log.info("No DTCs found.");
	                return result;
	            }

	            // Gerçek DTC kodlarını parse et
	            String[] codes = parseDTCs(resp);
	            for (String code : codes) {
	                Map<String, String> map = new HashMap<>();
	                map.put("code", code);
	                map.put("description", describeCode(code));
	                result.add(map);
	            }

	            history.save("UNKNOWN", "DTC Okuma", "Başarılı (" + result.size() + " kod bulundu)");
	            return result;

	        } catch (Exception e) {
	            log.error("DTC read error: {}", e.getMessage());
	            history.save("UNKNOWN", "DTC Okuma", "Hata: " + e.getMessage());
	            return result;
	        }
	    }

	    /**
	     * Mode 04 - Clear DTCs
	     */
	    public String clearDTCs() {
	        try {
	            String resp = elm.send("04"); // Mode 04 → Clear DTCs
	            history.save("UNKNOWN", "DTC Temizleme", "Başarılı");
	            return "Tüm arıza kodları silindi.";
	        } catch (Exception e) {
	            log.error("DTC clear error: {}", e.getMessage());
	            history.save("UNKNOWN", "DTC Temizleme", "Hata: " + e.getMessage());
	            return "Hata: " + e.getMessage();
	        }
	    }

	    // Basit DTC kod çözümleyici
	    private String[] parseDTCs(String raw) {
	        // Mock: "43 01 33 00 10" → ["P0133", "P0010"]
	        String[] parts = raw.replaceAll("[^0-9A-Fa-f]", " ").trim().split("\\s+");
	        List<String> list = new ArrayList<>();

	        for (int i = 0; i + 1 < parts.length; i += 2) {
	            try {
	                String A = parts[i];
	                String B = parts[i + 1];
	                int first = Integer.parseInt(A, 16);
	                int second = Integer.parseInt(B, 16);

	                String dtc = String.format("P%02X%02X", first, second);
	                list.add(dtc);
	            } catch (Exception ignored) {}
	        }
	        return list.toArray(new String[0]);
	    }

	    private String describeCode(String code) {
	        return switch (code) {
	            case "P0133" -> "O2 Sensor Circuit Slow Response (Bank 1 Sensor 1)";
	            case "P0171" -> "System Too Lean (Bank 1)";
	            case "P0420" -> "Catalyst Efficiency Below Threshold (Bank 1)";
	            case "P0300" -> "Random/Multiple Cylinder Misfire Detected";
	            default -> "Bilinmeyen arıza kodu";
	        };
	    }
}
