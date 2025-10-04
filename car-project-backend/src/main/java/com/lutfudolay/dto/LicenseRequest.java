package com.lutfudolay.dto;

public class LicenseRequest {

	private String licenseKey;
    private String owner;
    private String hardwareId;
    
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getHardwareId() { return hardwareId; }
    public void setHardwareId(String hardwareId) { this.hardwareId = hardwareId; }
}