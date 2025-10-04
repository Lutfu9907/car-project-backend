package com.lutfudolay.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lutfudolay.model.OperationHistory;
import com.lutfudolay.repository.OperationHistoryRepository;

@Service
public class OperationHistoryService {

	 @Autowired
	 private OperationHistoryRepository repository;

	 public OperationHistory save(String vin, String operationType, String result) {
		 OperationHistory history = new OperationHistory(vin, operationType, result);
		 return repository.save(history);
	 }

	 public List<OperationHistory> getAll() {
		 return repository.findAll();
	 }
}
