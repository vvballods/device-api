package com.ui.deviceapi.errors;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ApiError error;

    public ApiException(ApiError error) {
        super(error.getMessage());
        this.error = error;
    }

}
