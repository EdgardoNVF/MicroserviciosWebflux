package com.bancolombia.training.transactions.web;
import com.bancolombia.training.transactions.model.AccountVerification;
import com.bancolombia.training.transactions.model.Transaction;
import com.bancolombia.training.transactions.model.TransactionRequest;
import com.bancolombia.training.transactions.service.AccountLookupClient;
import com.bancolombia.training.transactions.service.TransactionCatalog;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class TransactionController {
  private final TransactionCatalog catalog;
  private final AccountLookupClient accountClient;
  private final boolean demoMode;

  public TransactionController(TransactionCatalog catalog, AccountLookupClient accountClient,
      @Value("${course.demo-delay-enabled:false}") boolean demoMode) {
    this.catalog = catalog;
    this.accountClient = accountClient;
    this.demoMode = demoMode;
  }

@PostMapping("/api/transactions")
@ResponseStatus (HttpStatus.CREATED)
public Mono<Transaction> create(
        @RequestBody Mono<TransactionRequest> request,
        ServerWebExchange exchange) {

    String correlationId = exchange.getAttributeOrDefault(
        CorrelationFilter.ATTRIBUTE,
        "not-set"
    );

    return request.flatMap(data ->
        catalog.createTransaction(data, correlationId)
    );
}

  @GetMapping("/api/transactions/account/{accountId}")
  public Flux<Transaction> forAccount(@PathVariable UUID accountId) {
    return catalog.forAccount(accountId);
  }

  /** Ruta didactica interna, NO forma parte del contrato publico bancario. */
  @GetMapping("/internal/verify-account/{accountId}")
  public Mono<AccountVerification> verify(@PathVariable UUID accountId,
      @RequestParam(name = "delayMs", defaultValue = "0") long delayMs,
      ServerWebExchange exchange) {
    if (delayMs < 0 || delayMs > 3000 || (delayMs != 0 && !demoMode)) {
      return Mono.error(new InvalidRequestException("delayMs solo se permite en dev (0 a 3000)"));
    }
    String correlationId = exchange.getAttributeOrDefault(CorrelationFilter.ATTRIBUTE, "not-set");
    return accountClient.findAccount(accountId, correlationId, delayMs)
        .map(account -> new AccountVerification(account.accountId(), true,
            account.status(), account.currency(), correlationId));
  }
}
