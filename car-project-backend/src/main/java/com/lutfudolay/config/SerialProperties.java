package com.lutfudolay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "obd.serial")
@Data
public class SerialProperties {

	private String portName;
    private int baudRate;
    private int readTimeoutMs;
}
