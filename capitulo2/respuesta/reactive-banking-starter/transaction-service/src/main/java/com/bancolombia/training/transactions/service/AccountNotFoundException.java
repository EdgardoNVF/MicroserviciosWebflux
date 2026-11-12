package com.bancolombia.training.transactions.service;
public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException() { 
    super("La cuenta solicitada no existe"); 
  }
}
