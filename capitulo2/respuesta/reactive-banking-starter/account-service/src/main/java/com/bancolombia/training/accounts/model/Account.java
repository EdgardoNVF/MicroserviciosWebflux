package com.bancolombia.training.accounts.model;
import java.time.Instant;
import java.util.UUID;

public record Account(
    UUID accountId, 
    String holderName, 
    String balance,                  
    String currency, 
    String status, 
    Instant createdAt) {}
