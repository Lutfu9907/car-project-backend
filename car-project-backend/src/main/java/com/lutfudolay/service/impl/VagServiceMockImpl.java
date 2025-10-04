package com.lutfudolay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.lutfudolay.service.IVagService;

@Service
@Primary // Varsayılan olarak mock çalışacak
public class VagServiceMockImpl implements IVagService{

	@Autowired
	private OperationHistoryService historyService;
	
	@Override
	public String enableCarplay(String vin) {
		
		// İşlem yapılıyor gibi davranalım
        String result = "CarPlay açıldı (mock)";
        historyService.save(vin, "CarPlay Açma", "Başarılı");
        return result;
	}

	@Override
	public String enableVideoInMotion(String vin) {
		
		String result = "Video In Motion açıldı (mock)";
        historyService.save(vin, "Video In Motion Açma", "Başarılı");
        return result;
	}
}
