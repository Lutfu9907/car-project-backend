package com.lutfudolay.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.lutfudolay.service.IVagService;

@Service
@Primary // Varsayılan olarak mock çalışacak
public class VagServiceMockImpl implements IVagService{

	@Override
	public String enableCarplay() {

		return "CarPlay açıldı (mock)";
	}

	@Override
	public String enableVideoInMotion() {

		return "Video In Motion açıldı (mock)";
	}

}
