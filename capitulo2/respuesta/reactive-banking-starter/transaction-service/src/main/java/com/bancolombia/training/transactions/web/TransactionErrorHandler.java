package com.bancolombia.training.transactions.web;
import com.bancolombia.training.transactions.service.AccountNotFoundException;
import com.bancolombia.training.transactions.service.AccountServiceUnavailableException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.codec.DecodingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class TransactionErrorHandler {
  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ApiError> missing(AccountNotFoundException exception, ServerWebExchange exchange) {
    return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", exception.getMessage(), exchange);
  }
  @ExceptionHandler(AccountServiceUnavailableException.class)
  public ResponseEntity<ApiError> unavailable(AccountServiceUnavailableException exception, ServerWebExchange exchange) {
    return error(HttpStatus.SERVICE_UNAVAILABLE, "ACCOUNT_SERVICE_UNAVAILABLE",
        "No se pudo consultar la cuenta", exchange);
  }
  @ExceptionHandler({InvalidRequestException.class, ServerWebInputException.class, DecodingException.class})
  public ResponseEntity<ApiError> invalid(Exception exception, ServerWebExchange exchange) {
    return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Solicitud invalida", exchange);
  }
  private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, ServerWebExchange exchange) {
    return ResponseEntity.status(status).body(new ApiError(code, message, status.value(),
        exchange.getAttributeOrDefault(CorrelationFilter.ATTRIBUTE, "not-set"), Instant.now()));
  }
}
