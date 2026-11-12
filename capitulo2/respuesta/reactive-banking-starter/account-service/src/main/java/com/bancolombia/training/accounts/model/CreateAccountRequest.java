package com.bancolombia.training.accounts.model;
public record CreateAccountRequest(
    String holderName,
    String currency) {}
