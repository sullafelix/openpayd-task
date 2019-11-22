package com.example.demo.model;

import lombok.Data;

@Data
public class ExchangeRate {
    private Currency base;
    private Currency target;
    private double rate;
}
