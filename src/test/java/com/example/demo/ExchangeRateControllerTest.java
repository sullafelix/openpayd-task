package com.example.demo;

import com.example.demo.model.ExchangeRate;
import com.example.demo.model.Transaction;
import com.example.demo.service.ExchangeRateLookupService;
import com.example.demo.service.TransactionService;
import com.example.demo.web.CustomErrorController;
import com.example.demo.web.ErrorHandlerControllerAdvice;
import com.example.demo.web.ExchangeRateController;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExchangeRateControllerTest {
    private MockMvc mockMvc;

    @InjectMocks
    private ExchangeRateController exchangeRateController;

    @Mock
    private ExchangeRateLookupService lookupService;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeRateController)
                    .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                    .setControllerAdvice(new ErrorHandlerControllerAdvice())
                    .build();
    }


    @Test
    public void rateTest() throws Exception {
        when(lookupService.getExchangeRate("USD", "TRY")).thenReturn(createUSDTRYExchangeRate());

        //Test
        ResultActions result = mockMvc.perform(get("/api/v1/rate")
                .param("base", "USD")
                .param("target", "TRY")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.base", is("USD")))
              .andExpect(jsonPath("$.rates.TRY", is(5.7)));
    }

    @Test
    public void convertTest() throws Exception {
        when(lookupService.saveTransaction("USD", "TRY", BigDecimal.valueOf(20)))
                .thenReturn(createUSDTRYTransaction());
        //Test
        ResultActions result = mockMvc.perform(get("/api/v1/convert")
                .param("base", "USD")
                .param("target", "TRY")
                .param("amount", "20")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk()).andExpect(jsonPath("$.amount", is(114)));
    }

    @Test
    public void transactionsTest() throws Exception {
        when(transactionService.getTransactionsByDateTime(eq(LocalDate.of(2019, 11, 26)), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(createTransactions());
        //Test
        ResultActions result = mockMvc.perform(get("/api/v1/conversions")
                .param("date", "2019-11-26")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].base", is("USD")))
            .andExpect(jsonPath("$.content[0].target", is("TRY")))
            .andExpect(jsonPath("$.content[0].amount", is(114)))
            .andExpect(jsonPath("$.content[0].date[0]", is(2019))) // year
            .andExpect(jsonPath("$.content[0].date[1]", is(11))) // month
            .andExpect(jsonPath("$.content[0].date[2]", is(26))); //day
    }

    @Test
    public void transactionsFailTest() throws Exception {
        //Test
        ResultActions result = mockMvc.perform(get("/api/v1/conversions")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
        result.andExpect(status().is5xxServerError());
    }

    private Page<Transaction> createTransactions() {
        List<Transaction> transactions = Arrays.asList(createUSDTRYTransaction());
        return new PageImpl<>(transactions);
    }

    private Transaction createUSDTRYTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setBase(Currency.getInstance("USD"));
        transaction.setTarget(Currency.getInstance("TRY"));
        transaction.setAmount(BigDecimal.valueOf(114));
        transaction.setDate(LocalDateTime.of(2019, 11, 26, 0, 0,0));

        return transaction;
    }

    private ExchangeRate createUSDTRYExchangeRate() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBase(Currency.getInstance("USD"));
        Map<Currency, BigDecimal> currencyMap = new HashMap<>();
        currencyMap.put(Currency.getInstance("TRY"), BigDecimal.valueOf(5.70));
        exchangeRate.setRates(currencyMap);

        return exchangeRate;
    }
}
