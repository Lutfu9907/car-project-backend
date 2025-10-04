package com.lutfudolay.service.impl;

import com.lutfudolay.service.IVagService;

public class VagServiceRealImpl implements IVagService {

	@Override
    public String enableCarplay(String vin) {
        throw new UnsupportedOperationException("Gerçek CarPlay açma işlemi henüz implement edilmedi.");
    }

    @Override
    public String enableVideoInMotion(String vin) {
        throw new UnsupportedOperationException("Gerçek Video In Motion işlemi henüz implement edilmedi.");
    }
}