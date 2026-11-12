package com.bancolombia.training.transactions.service;
public class AccountServiceUnavailableException extends RuntimeException {
  public AccountServiceUnavailableException(Throwable cause) {
    super("Account Service no esta disponible", cause);
  }
}
