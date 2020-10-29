package com.razorthink.engine.exception;


public class BadRequestException extends RuntimeException {
    private String message;

    public BadRequestException( String message) {
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

}
