package com.example.demo.exceptions;

import java.util.List;

public class ParameterMissingException extends RuntimeException {
    private static final String ERROR_MSG = "Mandatory parameters missing: ";

    public ParameterMissingException(List<String> missingInputs) {
        super(ERROR_MSG + String.join(",", missingInputs));
    }
}
