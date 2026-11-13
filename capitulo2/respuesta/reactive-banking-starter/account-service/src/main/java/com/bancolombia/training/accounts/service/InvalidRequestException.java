package com.bancolombia.training.accounts.service;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message){
       super(message);
    }

}
