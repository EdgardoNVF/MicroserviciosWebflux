package com.bancolombia.training.accounts.service;
public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException() { 
    super("La cuenta solicitada no existe"); 
  }
}
