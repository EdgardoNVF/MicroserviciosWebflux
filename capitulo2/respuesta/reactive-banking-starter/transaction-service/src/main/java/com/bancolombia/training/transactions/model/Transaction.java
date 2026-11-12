package com.bancolombia.training.transactions.model;
import java.time.Instant;
import java.util.UUID;
public record Transaction(
    UUID transactionId, 
    UUID accountId, 
    String type, 
    String amount,
    String currency, 
    String status, 
    Instant createdAt) {}
