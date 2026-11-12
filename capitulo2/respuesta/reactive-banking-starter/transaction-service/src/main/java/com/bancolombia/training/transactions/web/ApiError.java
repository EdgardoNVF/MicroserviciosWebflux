package com.bancolombia.training.transactions.web;
import java.time.Instant;
public record ApiError(
    String code, 
    String message, 
    int status, 
    String correlationId, 
    Instant timestamp) {}
