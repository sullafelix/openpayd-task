package com.example.demo.exceptions;

public class FaultyCurrencyCodeException extends RuntimeException {
    private static final String ERROR_MSG = "The currency code is could not be found!";

    public FaultyCurrencyCodeException() {
        super(ERROR_MSG);
    }

    public FaultyCurrencyCodeException(Exception inner) {
        super(ERROR_MSG, inner);
    }
}
