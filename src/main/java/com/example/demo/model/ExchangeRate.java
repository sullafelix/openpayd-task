package com.example.demo.model;

import lombok.Data;

import java.util.Currency;
import java.util.Map;

@Data
public class ExchangeRate {
    private Currency base;
    private Map<Currency, Double> rates;
}
