package com.example.demo.web;

import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import com.example.demo.service.ExchangeRateLookupService;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RequestMapping("api/v1")
@RestController
public class ExchangeRateController {

    private ExchangeRateLookupService exchangeRateLookupService;
    private TransactionService transactionService;

    @Autowired
    public ExchangeRateController(ExchangeRateLookupService exchangeRateLookupService,
                                  TransactionService transactionService) {
        this.exchangeRateLookupService = exchangeRateLookupService;
        this.transactionService = transactionService;
    }

    @GetMapping("rate")
    public ExchangeRate rate(@RequestParam(value = "base", required = true) String baseCode,
                                        @RequestParam(value = "target", required = true) String targetCode) {
        return exchangeRateLookupService.getExchangeRate(baseCode, targetCode);
    }

    @GetMapping("convert")
    public Transaction convert(@RequestParam(value = "base", required = true) String baseCode,
                               @RequestParam(value = "target", required = true) String targetCode,
                               @RequestParam(value = "amount", required = true) Double amount) {
        Transaction transaction = exchangeRateLookupService.getTransaction(baseCode, targetCode, amount);
        Transaction response = new Transaction();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());

        return response;
    }

    @GetMapping("conversions")
    private List<Transaction> transactions() {
        return transactionService.getTransactions();
    }
}
