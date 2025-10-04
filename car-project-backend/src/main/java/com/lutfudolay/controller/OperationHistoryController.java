package com.lutfudolay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lutfudolay.model.OperationHistory;
import com.lutfudolay.service.impl.OperationHistoryService;

@RestController
@RequestMapping("/api/history")
public class OperationHistoryController {

	@Autowired
    private OperationHistoryService service;

    @GetMapping
    public ResponseEntity<List<OperationHistory>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
