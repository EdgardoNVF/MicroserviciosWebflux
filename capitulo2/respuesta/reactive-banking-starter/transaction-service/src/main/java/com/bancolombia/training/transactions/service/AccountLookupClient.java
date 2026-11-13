package com.bancolombia.training.transactions.service;

import com.bancolombia.training.transactions.model.AccountView;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AccountLookupClient {
  private static final Logger log = LoggerFactory.getLogger(AccountLookupClient.class);
  private final WebClient client;
  private final Duration timeout;

  public AccountLookupClient(WebClient accountWebClient,
      @Value("${services.account.timeout-ms:500}") long timeoutMs) {
    this.client = accountWebClient;
    if (timeoutMs <= 0) throw new IllegalArgumentException("timeout-ms debe ser positivo");
    this.timeout = Duration.ofMillis(timeoutMs);
  }

  public Mono<AccountView> findAccount(UUID id, String correlationId, long demoDelayMs) {
   
    return client.get()
    .uri(builder->{
      builder.path("/api/accounts/{id}");
      if(demoDelayMs>0) builder.queryParam("delayMs", demoDelayMs);
      return builder.build(id);
    })
    .header("X-Correlation-Id",correlationId)
    .retrieve()
    // HTTP de Account Service: conservar la distincion entre autorizacion y disponibilidad.
    .onStatus(status -> status.value() == 401,
        response -> Mono.error(new AccountServiceUnauthorizedException()))
    .onStatus(status -> status.value() == 403,
        response -> Mono.error(new AccountServiceForbiddenException()))
    .onStatus(status->status.value() == 404, response-> Mono.error(new AccountNotFoundException()))
    .bodyToMono(AccountView.class)
    .timeout(timeout)
    .onErrorMap(error-> !(error instanceof AccountNotFoundException)
    && !(error instanceof AccountServiceUnauthorizedException)
    && !(error instanceof AccountServiceForbiddenException)
    && !(error instanceof AccountServiceUnavailableException),
      AccountServiceUnavailableException::new
    );
  }
}
