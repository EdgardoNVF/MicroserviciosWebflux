package com.bancolombia.training.accounts.service;

import com.bancolombia.training.accounts.model.Account;
import com.bancolombia.training.accounts.model.CreateAccountRequest;
import com.bancolombia.training.accounts.web.InvalidRequestException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AccountCatalog {
  public static final UUID DEMO_ACCOUNT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();

  public AccountCatalog() {
    accounts.put(DEMO_ACCOUNT_ID, new Account(DEMO_ACCOUNT_ID, "Cliente Demostracion",
        "150000.00", "COP", "ACTIVE", Instant.parse("2026-11-04T15:00:00Z")));
  }

  public Mono<Account> find(UUID accountId) {
    return Mono.defer(() -> Mono.justOrEmpty(accounts.get(accountId))
        .switchIfEmpty(Mono.error(new AccountNotFoundException())));
  }

  public Mono<Account> create(CreateAccountRequest request) {
    
    return Mono.fromSupplier(() -> {
    if (request == null || request.holderName() == null
        || request.holderName().isBlank() || request.holderName().length() > 120) {
        throw new InvalidRequestException("holderName debe contener entre 1 y 120 caracteres");
    }
    if (!"COP".equals(request.currency())) {
        throw new InvalidRequestException("Solo se admite currency=COP en este laboratorio");
    }
    UUID id = UUID.randomUUID();
    Account created = new Account(id, request.holderName().trim(), "0.00",
        "COP", "ACTIVE", Instant.now());
    accounts.put(id, created);
    return created;
});
  }
}
