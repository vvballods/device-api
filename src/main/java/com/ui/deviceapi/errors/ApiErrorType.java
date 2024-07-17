package com.ui.deviceapi.errors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public interface ApiErrorType {
    ApiError MAC_ADDRESS_ALREADY_IN_USE = new ApiError("Mac Address already in use", BAD_REQUEST);
    ApiError DEVICE_NOT_FOUND = new ApiError("Device not found", NOT_FOUND);

}
