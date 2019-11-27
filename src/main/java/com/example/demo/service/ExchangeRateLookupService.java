package com.example.demo.service;

import com.example.demo.exceptions.FaultyCurrencyCodeException;
import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Service
public class ExchangeRateLookupService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateLookupService.class);

    @Value("${task.currencyservice.url}")
    private String url;

    private final RestTemplate restTemplate;
    private final TransactionService transactionService;

    @Autowired
    public ExchangeRateLookupService(RestTemplate restTemplate, TransactionService transactionService) {
        this.restTemplate = restTemplate;
        this.transactionService = transactionService;
    }

    public ExchangeRate getExchangeRate(String baseCode, String targetCode) {
        Currency baseCurrency = getCurrencyFromCode(baseCode);
        Currency targetCurrency = getCurrencyFromCode(targetCode);
        return getExchangeRate(baseCurrency, targetCurrency);
    }



    private ExchangeRate getExchangeRate(Currency base, Currency target) {
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

    public Transaction convert(String baseCode, String targetCode, BigDecimal baseAmount) {
        ExchangeRate exchangeRate = getExchangeRate(baseCode, targetCode);
        Currency targetCurrency = getCurrencyFromCode(targetCode);

        Transaction transaction = new Transaction();
        transaction.setBase(exchangeRate.getBase());
        transaction.setTarget(targetCurrency);
        transaction.setAmount(baseAmount.multiply(exchangeRate.getRates().get(targetCurrency)));
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
