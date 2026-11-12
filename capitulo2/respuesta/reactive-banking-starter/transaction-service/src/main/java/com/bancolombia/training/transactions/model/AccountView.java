package com.bancolombia.training.transactions.model;
import java.util.UUID;
/** DTO local de contrato externo: no se importa el modelo Java del otro servicio. */
public record AccountView(
    UUID accountId, 
    String holderName, 
    String balance,
    String currency, 
    String status, 
    java.time.Instant createdAt) {}
