package com.lutfudolay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;
import com.lutfudolay.config.SerialProperties;

@Service
public class ElmServiceImpl {
	
	@Autowired
    private SerialProperties props;

    @Value("${app.mockMode:false}")
    private boolean mockMode;

    private SerialPort port;

    public String[] listPorts() {
        return java.util.Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toArray(String[]::new);
    }

    public void connect() {
        if (port != null && port.isOpen()) return;

        port = SerialPort.getCommPort(props.getPortName());
        port.setBaudRate(props.getBaudRate());
        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                props.getReadTimeoutMs(),
                props.getReadTimeoutMs()
        );

        if (!port.openPort()) {
            if (mockMode) {
                System.out.println("Mock mode aktif: cihaz yok, port açılamadı.");
                return; // hata atmıyoruz → mock çalışacak
            } else {
                throw new IllegalStateException("Port açılamadı: " + props.getPortName());
            }
        }

        send("ATZ");
        send("ATE0");
        send("ATL0");
        send("ATS0");
        send("ATH1");
        send("ATSP6");
    }

    public String send(String cmd) {
        if (port == null || !port.isOpen()) {
            if (mockMode) {
                return "MOCK_RESPONSE"; // cihaz yokken sahte cevap
            }
            throw new IllegalStateException("Port bağlı değil");
        }

        String payload = cmd.endsWith("\r") ? cmd : cmd + "\r";
        port.writeBytes(payload.getBytes(), payload.length());

        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[256];
        try { Thread.sleep(80); } catch (InterruptedException ignored) {}
        while (port.bytesAvailable() > 0) {
            int n = port.readBytes(buf, buf.length);
            if (n > 0) sb.append(new String(buf, 0, n));
            if (sb.indexOf(">") >= 0) break;
        }
        return sb.toString();
    }

    public int readRpm() {
        if (mockMode && (port == null || !port.isOpen())) {
            return 780; // cihaz yokken mock RPM
        }

        String resp = send("010C");
        String hex = resp.replaceAll("[^0-9A-Fa-f ]", " ");
        String[] parts = hex.trim().split("\\s+");
        if (parts.length >= 2) {
            int A = Integer.parseInt(parts[parts.length - 2], 16);
            int B = Integer.parseInt(parts[parts.length - 1], 16);
            return ((A * 256) + B) / 4;
        }
        throw new IllegalStateException("RPM yanıtı çözülemedi: " + resp);
    }
 }


