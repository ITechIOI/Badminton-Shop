package com.example.reportservice.utils;

public class ResponseStatusException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ResponseStatusException(String message){
        super(message);
    }
}
