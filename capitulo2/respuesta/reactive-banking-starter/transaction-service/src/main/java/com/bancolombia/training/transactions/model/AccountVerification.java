package com.bancolombia.training.transactions.model;
import java.util.UUID;
public record AccountVerification(
    UUID accountId, 
    boolean exists, 
    String accountStatus,
    String currency, 
    String correlationId) {}
