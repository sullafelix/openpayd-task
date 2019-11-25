package com.example.demo.service;

import com.example.demo.exceptions.FaultyCurrencyCodeException;
import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

@Service
public class ExchangeRateLookupService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateLookupService.class);

    @Value("${task.currencyservice.url}")
    private String url;

    private final RestTemplate restTemplate;
    private final TransactionService transactionService;

    @Autowired
    public ExchangeRateLookupService(RestTemplateBuilder restTemplateBuilder, TransactionService transactionService) {
        this.restTemplate = restTemplateBuilder.build();
        this.transactionService = transactionService;
    }

    public ExchangeRate getExchangeRate(String baseCode, String targetCode) {
        Currency baseCurrency = getCurrencyFromCode(baseCode);
        Currency targetCurrency = getCurrencyFromCode(targetCode);
        return getExchangeRate(baseCurrency, targetCurrency);
    }



    public ExchangeRate getExchangeRate(Currency base, Currency target) {
        logger.info("Looking up base:" + base + " target:" + target);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                                        .queryParam("base", base)
                                        .queryParam("symbols", target);
        ResponseEntity<ExchangeRate> response = restTemplate.exchange(builder.toUriString(),
                                                     HttpMethod.GET,
                                                     new HttpEntity<>(headers),
                                                     ExchangeRate.class);

        return response.getBody();
    }

    public Transaction getTransaction(String baseCode, String targetCode, double baseAmount) {
        Currency baseCurrency = getCurrencyFromCode(baseCode);
        Currency targetCurrency = getCurrencyFromCode(targetCode);
        ExchangeRate exchangeRate = getExchangeRate(baseCurrency, targetCurrency);

        Transaction transaction = new Transaction();
        transaction.setBase(baseCurrency);
        transaction.setTarget(targetCurrency);
        transaction.setAmount(baseAmount * exchangeRate.getRates().get(targetCurrency));
        transaction.setDate(LocalDateTime.now());
        transaction = transactionService.saveTransaction(transaction);

        return transaction;
    }

    private Currency getCurrencyFromCode(String currencyCode) {
        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new FaultyCurrencyCodeException();
        }
        return currency;
    }
}
