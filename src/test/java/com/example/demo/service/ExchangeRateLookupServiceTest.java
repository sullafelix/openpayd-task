package com.example.demo.service;

import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExchangeRateLookupServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ExchangeRateLookupService exchangeRateLookupService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(restTemplate.exchange(
                any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(getUSDtoTRYExchangeRate(), HttpStatus.OK));
        ReflectionTestUtils.setField(exchangeRateLookupService, "url", "http://foo");

        when(transactionService.saveTransaction(any(Transaction.class)))
                .thenAnswer(t -> {
                    ((Transaction)t.getArgument(0)).setId(UUID.randomUUID());
                    return t;
                });
    }

    @Test
    public void getExchangeRateTest() {
        ExchangeRate exchangeRate = exchangeRateLookupService.getExchangeRate("USD", "TRY");
        Assertions.assertNotNull(exchangeRate);
        Assertions.assertEquals(exchangeRate.getBase(), Currency.getInstance("USD"));
        Assertions.assertEquals(exchangeRate.getRates().size(), 1);
        Assertions.assertEquals(exchangeRate.getRates().get(Currency.getInstance("TRY")), BigDecimal.valueOf(5.70));
    }

    @Test
    public void convertTest() {
        Transaction transaction = exchangeRateLookupService.convert("USD", "TRY", BigDecimal.valueOf(20));
        Assertions.assertNotNull(transaction);
        Assertions.assertNotNull(transaction.getId());
        Assertions.assertEquals(transaction.getAmount(), BigDecimal.valueOf(114));
        Assertions.assertEquals(transaction.getBase(), Currency.getInstance("USD"));
        Assertions.assertEquals(transaction.getTarget(), Currency.getInstance("TRY"));
        Assertions.assertEquals(transaction.getDate().toLocalDate(), LocalDate.now());
    }

    private ExchangeRate getUSDtoTRYExchangeRate() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBase(Currency.getInstance("USD"));
        Map<Currency, BigDecimal> rates = new HashMap<>();
        rates.put(Currency.getInstance("TRY"), BigDecimal.valueOf(5.70));
        exchangeRate.setRates(rates);

        return exchangeRate;
    }
}
