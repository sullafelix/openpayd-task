package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Transaction> getTransactions() {
        return repository.findAll();
    }
}
