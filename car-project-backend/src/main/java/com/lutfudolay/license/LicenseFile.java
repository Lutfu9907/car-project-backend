package com.lutfudolay.license;

public class LicenseFile {

	private LicensePayload payload;
    private String signature;

    public LicensePayload getPayload() { return payload; }
    public void setPayload(LicensePayload payload) { this.payload = payload; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
