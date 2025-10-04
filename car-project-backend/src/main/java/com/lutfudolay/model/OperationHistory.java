package com.lutfudolay.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operation_history")
@Getter
@Setter
@NoArgsConstructor
public class OperationHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String vin;               // aracın şasi numarası
    private String operationType;     // yapılan işlem (ör: "CarPlay Açıldı", "OBD Connect")
    private String result;            // işlem sonucu (ör: "Başarılı", "Hata")
    private LocalDateTime timestamp;
    
    public OperationHistory(String vin, String operationType, String result) {
        this.vin = vin;
        this.operationType = operationType;
        this.result = result;
        this.timestamp = LocalDateTime.now();
    }
}
