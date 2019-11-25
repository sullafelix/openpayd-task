package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Service
public class TransactionService {
    private TransactionRepository repository;

    @Autowired
    public TransactionService(TransactionRepository repository){
        this.repository = repository;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return repository.save(transaction);
    }

    public Page<Transaction> getTransactions(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Transaction> getTransactionsByDateTime(LocalDate timestamp, Pageable pageable) {
        return repository.findByDateBetween(timestamp.atTime(0,0,0,0),
                                            timestamp.plusDays(1).atTime(0,0,0,0),
                pageable);
    }

    public Page<Transaction> getTransactionById(UUID transactionId, Pageable pageable) {
        return repository.findByTransactionId(transactionId, pageable);
    }
}
