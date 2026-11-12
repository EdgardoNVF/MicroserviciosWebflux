package com.bancolombia.training.transactions.web;
public class InvalidRequestException extends RuntimeException {
  public InvalidRequestException(String message) { super(message); }
}
