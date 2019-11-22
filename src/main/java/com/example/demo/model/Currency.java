package com.example.demo.model;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class Currency {
    private String symbol;
}
