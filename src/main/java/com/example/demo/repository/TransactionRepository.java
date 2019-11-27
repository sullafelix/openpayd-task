package com.example.demo.repository;

import com.example.demo.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("select c from Transaction c where c.date between :startDate AND :endDate")
    Page<Transaction> findByDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("select c from Transaction c where c.id = :id")
    Page<Transaction> findByTransactionId(@Param("id") UUID id, Pageable pageable);
}
