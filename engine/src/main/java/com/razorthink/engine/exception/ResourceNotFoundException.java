package com.razorthink.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResourceNotFoundException extends RuntimeException {
    private String message;

    public ResourceNotFoundException( String message) {
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

}
