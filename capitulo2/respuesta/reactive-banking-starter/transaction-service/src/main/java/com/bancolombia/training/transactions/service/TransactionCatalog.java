package com.bancolombia.training.transactions.service;
import com.bancolombia.training.transactions.model.Transaction;
import com.bancolombia.training.transactions.model.TransactionRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TransactionCatalog {
  private static final Logger LOGGER=LoggerFactory.getLogger(TransactionCatalog.class);
  private final AccountLookupClient account;
  private final Map<UUID, Transaction> transactions;

  public TransactionCatalog(AccountLookupClient account){
      this.account=account;
      this.transactions= new ConcurrentHashMap<>();
      this.transactions.put(
        UUID.fromString("22222222-2222-4222-8222-222222222221"),
        new Transaction(UUID.fromString("22222222-2222-4222-8222-222222222221"),
          UUID.fromString("11111111-1111-4111-8111-111111111111"),
          "DEPOSIT", "10000.00", "COP", "COMPLETED", Instant.parse("2026-11-04T15:05:00Z"))
      );

      this.transactions.put(
        UUID.fromString("22222222-2222-4222-8222-222222222222"), 
        new Transaction(UUID.fromString("22222222-2222-4222-8222-222222222222"),
          UUID.fromString("11111111-1111-4111-8111-111111111111"),
          "WITHDRAWAL", "5000.00", "COP", "COMPLETED", Instant.parse("2026-11-04T15:10:00Z"))
        );
  }

  public Flux<Transaction> forAccount(UUID accountId) {
    return Flux.defer(() ->
        Flux.concat(
            Flux.fromIterable(transactions.values())
        )
        .filter(t -> t.accountId().equals(accountId))
    );
}
 
public Mono<Transaction> createTransaction(
        TransactionRequest request,
        String correlationId) {

    return Mono.defer(() -> {

        // Validar solicitud
        if (request == null
                || request.accountId() == null
                || request.type() == null
                || request.amount() == null
                || !"COP".equals(request.currency())) {

            return Mono.error(
                new IllegalArgumentException(
                    "Datos de transacción inválidos"
                )
            );
        }

        // Validar monto
        BigDecimal amount;

        try {
            amount = new BigDecimal(request.amount());
        } catch (NumberFormatException ex) {
            return Mono.error(
                new IllegalArgumentException("Monto inválido")
            );
        }

        if (amount.signum() <= 0 || amount.scale() > 2) {
            return Mono.error(
                new IllegalArgumentException(
                    "El monto debe ser positivo y tener máximo 2 decimales"
                )
            );
        }

        // Consultar la cuenta mediante WebClient
        return account.findAccount(
                request.accountId(),
                correlationId,
                0
            )
            .flatMap(accountView -> {
                
                LOGGER.info("WEBCLIENT RESPONSE: {}",accountView.toString());

                // Validar estado de la cuenta
                if (!"ACTIVE".equals(accountView.status())) {
                    return Mono.error(
                        new IllegalStateException(
                            "La cuenta no está activa"
                        )
                    );
                }

                // Crear transacción pendiente
                Transaction transaction = new Transaction(
                    UUID.randomUUID(),
                    request.accountId(),
                    request.type().name(),
                    amount.toPlainString(),
                    "COP",
                    "PENDING",
                    Instant.now()
                );

                transactions.put(
                    transaction.transactionId(),
                    transaction
                );

                LOGGER.info(
                    "Transacción registrada id={} accountId={} status=PENDING",
                    transaction.transactionId(),
                    transaction.accountId()
                );

                return Mono.just(transaction);
            });
    });
}

}
