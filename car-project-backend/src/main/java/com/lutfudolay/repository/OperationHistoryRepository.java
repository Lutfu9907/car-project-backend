package com.lutfudolay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lutfudolay.model.OperationHistory;

@Repository
public interface OperationHistoryRepository extends JpaRepository<OperationHistory, Long>{

}
