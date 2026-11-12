package com.bancolombia.training.accounts.web;
import java.time.Instant;
public record ApiError(
    String code, 
    String message, 
    int status, 
    String correlationId, 
    Instant timestamp) {}
