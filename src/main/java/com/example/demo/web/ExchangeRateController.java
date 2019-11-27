package com.example.demo.web;

import com.example.demo.exceptions.ParameterMissingException;
import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import com.example.demo.service.ExchangeRateLookupService;
import com.example.demo.service.TransactionService;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation("This service call is used to get the exchange rate, currency pair input is given as query parameters, 'base' and 'target'")
    public ExchangeRate rate(@RequestParam(value = "base", required = true) String baseCode,
                             @RequestParam(value = "target", required = true) String targetCode) {
        return exchangeRateLookupService.getExchangeRate(baseCode, targetCode);
    }

    @GetMapping("convert")
    @ApiOperation("This service call is used to convert an amount in source currency to the amount in target currency")
    public Transaction convert(@RequestParam(value = "base", required = true) String baseCode,
                               @RequestParam(value = "target", required = true) String targetCode,
                               @RequestParam(value = "amount", required = true) BigDecimal amount) {
        Transaction transaction = exchangeRateLookupService.convert(baseCode, targetCode, amount);
        Transaction response = new Transaction();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());

        return response;
    }

    @GetMapping("conversions")
    @ApiOperation("This service call is used to get the list of conversions performed. Either a transaction id or " +
            "transaction date must be given as query parameter parameters 'transactionId' and 'date' respectively. " +
            "Paging is controlled by the page query parameter, starting with 0 i.e. '?page=0'")
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
