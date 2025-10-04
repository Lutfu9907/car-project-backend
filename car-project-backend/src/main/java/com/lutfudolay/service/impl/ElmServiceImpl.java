package com.lutfudolay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;
import com.lutfudolay.config.SerialProperties;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ElmServiceImpl {
	
	@Autowired
    private SerialProperties props;

    @Autowired
    private OperationHistoryService historyService;

    @Value("${app.mockMode:false}")
    private boolean mockMode;

    private SerialPort port;

    // 1. Port listesini döndür
    public String[] listPorts() {
        return java.util.Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toArray(String[]::new);
    }

    // 2. Bağlantı kur
    public void connect() {
    	try {
            if (mockMode) {
                log.info("Mock mode: OBD bağlantısı simüle ediliyor...");
                String mockVin = "WVWZZZ1JZXW000000";
                historyService.save(mockVin, "OBD Bağlantı Kuruldu", "Başarılı (Mock)");

                // VIN'i de mock olarak kaydet
                historyService.save(mockVin, "VIN Okuma", "Başarılı (Mock)");
                log.info("Mock VIN: {}", mockVin);
                return;
            }

            port = SerialPort.getCommPort(props.getPortName());
            port.setBaudRate(props.getBaudRate());
            port.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                    props.getReadTimeoutMs(),
                    props.getReadTimeoutMs()
            );

            if (!port.openPort()) {
                historyService.save("UNKNOWN", "OBD Bağlantı", "Hata: Port açılamadı (" + props.getPortName() + ")");
                throw new IllegalStateException("Port açılamadı: " + props.getPortName());
            }

            // ELM327 başlatma komutları
            send("ATZ");
            send("ATE0");
            send("ATL0");
            send("ATS0");
            send("ATH1");
            send("ATSP6");

            log.info("Gerçek OBD bağlantısı kuruldu -> {}", props.getPortName());
            historyService.save("REAL_VIN", "OBD Bağlantı Kuruldu", "Başarılı");

            // 🔹 Bağlantıdan sonra otomatik VIN okuma
            try {
                String vin = readVin();
                historyService.save(vin, "VIN Okuma", "Bağlantı sonrası otomatik okundu");
                log.info("Bağlantı sonrası VIN: {}", vin);
            } catch (Exception vinEx) {
                historyService.save("UNKNOWN", "VIN Okuma", "Bağlantı sonrası hata: " + vinEx.getMessage());
                log.warn("Bağlantı sonrası VIN okunamadı: {}", vinEx.getMessage());
            }

        } catch (Exception e) {
            historyService.save("UNKNOWN", "OBD Bağlantı", "Hata: " + e.getMessage());
            log.error("OBD bağlantısı kurulamadı: {}", e.getMessage());
            throw e;
        }
    }

    // 3. Komut gönder
    public String send(String cmd) {
    	// 1) Mock davranışı — komuta göre daha gerçekçi cevaplar
        if (mockMode) {
            log.debug("Mock send komutu: {}", cmd);
            return switch (cmd) {
                case "010C" -> "41 0C 1E A8 >"; // sahte RPM (örn. 0x1E 0xA8)
                case "0902" -> "49 02 01 57 56 57 5A 5A 5A 31 4A 5A 58 57 30 30 30 30 30 30 >"; // sahte VIN -> WVWZZZ1JZXW000000
                default -> "OK >";
            };
        }

        // 2) Gerçek cihaz: port kontrolü
        if (port == null || !port.isOpen()) {
            throw new IllegalStateException("Port bağlı değil");
        }

        // 3) Komutu gönder
        String payload = cmd.endsWith("\r") ? cmd : cmd + "\r";
        port.writeBytes(payload.getBytes(), payload.length());
        log.debug("Komut gönderildi: {}", cmd);

        // 4) Cevabı oku — '>' ile bitiş beklenir, ama timeout kontrolü eklenir
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[256];
        final long start = System.currentTimeMillis();
        final long timeoutMs = (props != null ? props.getReadTimeoutMs() : 1000); // fallback 1s

        try { Thread.sleep(80); } catch (InterruptedException ignored) {}

        while (true) {
            // zaman aşımı kontrolü
            if (System.currentTimeMillis() - start > timeoutMs) {
                log.warn("Read timeout ({} ms) for command {}", timeoutMs, cmd);
                break;
            }

            int available = port.bytesAvailable();
            if (available <= 0) {
                // ufak bekleme, cihaz yanıt yaratıyor olabilir
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                continue;
            }

            int n = port.readBytes(buf, Math.min(buf.length, available));
            if (n > 0) sb.append(new String(buf, 0, n));

            if (sb.indexOf(">") >= 0) break;
        }

        String response = sb.toString().trim();
        if (response.isEmpty()) {
            log.debug("Cevap boş geldi, NO_RESPONSE döndürülüyor. Komut: {}", cmd);
            return "NO_RESPONSE";
        }

        log.debug("Cevap alındı: {}", response);
        return response;
    }

    // 4. RPM okuma
    public int readRpm() {
        try {
            if (mockMode) {
                int mockRpm = 780;
                historyService.save("MOCK_VIN", "RPM Okuma", "Başarılı (Mock) -> " + mockRpm);
                return mockRpm;
            }

            if (port == null || !port.isOpen()) {
                historyService.save("UNKNOWN", "RPM Okuma", "Hata: Port bağlı değil");
                throw new IllegalStateException("Port bağlı değil");
            }

            String resp = send("010C"); // PID 010C -> RPM
            String hex = resp.replaceAll("[^0-9A-Fa-f ]", " ");
            String[] parts = hex.trim().split("\\s+");
            if (parts.length >= 2) {
                int A = Integer.parseInt(parts[parts.length - 2], 16);
                int B = Integer.parseInt(parts[parts.length - 1], 16);
                int rpm = ((A * 256) + B) / 4;

                historyService.save("REAL_VIN", "RPM Okuma", "Başarılı -> " + rpm);
                log.info("RPM okundu: {}", rpm);
                return rpm;
            } else {
                historyService.save("UNKNOWN", "RPM Okuma", "Hata: Yanıt çözülemedi");
                throw new IllegalStateException("RPM yanıtı çözülemedi: " + resp);
            }
        } catch (Exception e) {
            historyService.save("UNKNOWN", "RPM Okuma", "Hata: " + e.getMessage());
            log.error("RPM okuma hatası: {}", e.getMessage());
            throw e;
        }
    }
    
    public String readVin() {
        try {
            if (mockMode) {
                String vin = "WVWZZZ1JZXW000000";
                historyService.save(vin, "VIN Okuma", "Başarılı (Mock)");
                log.info("Mock VIN: {}", vin);
                return vin;
            }

            if (port == null || !port.isOpen()) {
                historyService.save("UNKNOWN", "VIN Okuma", "Hata: Port bağlı değil");
                throw new IllegalStateException("Port bağlı değil");
            }

            String response = send("0902"); // Mode 09 PID 02 → VIN
            // VIN verisini temizle
            String cleaned = response
                    .replaceAll("[^0-9A-Za-z]", "")
                    .replace("SEARCHING", "")
                    .replace("BUSINIT", "")
                    .trim();

            // VIN genelde 17 karakterdir
            String vin = cleaned.length() >= 17
                    ? cleaned.substring(cleaned.length() - 17)
                    : "VIN_BULUNAMADI";

            historyService.save(vin, "VIN Okuma", "Başarılı");
            log.info("VIN okundu: {}", vin);
            return vin;

        } catch (Exception e) {
            historyService.save("UNKNOWN", "VIN Okuma", "Hata: " + e.getMessage());
            log.error("VIN okuma hatası: {}", e.getMessage());
            throw e;
        }
    }
 }


