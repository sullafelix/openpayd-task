package com.example.demo.web;

import com.example.demo.exceptions.ParameterMissingException;
import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import com.example.demo.service.ExchangeRateLookupService;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
                               @RequestParam(value = "amount", required = true) BigDecimal amount) {
        Transaction transaction = exchangeRateLookupService.saveTransaction(baseCode, targetCode, amount);
        Transaction response = new Transaction();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());

        return response;
    }

    @GetMapping("conversions")
    private Page<Transaction> transactions(
            @RequestParam(value = "transactionId", required = false) UUID transactionId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Pageable pageable) {
        if(transactionId == null && date == null) {
            throw new ParameterMissingException(Arrays.asList("transactionId", "date"));
        }
        if(date != null) {
            return transactionService.getTransactionsByDateTime(date, pageable);
        }
        return transactionService.getTransactionById(transactionId, pageable);
    }
}
