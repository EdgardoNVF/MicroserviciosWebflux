package com.bancolombia.training.accounts.web;

import com.bancolombia.training.accounts.model.Account;
import com.bancolombia.training.accounts.model.CreateAccountRequest;
import com.bancolombia.training.accounts.service.AccountCatalog;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
  private final AccountCatalog catalog;
  private final boolean demoDelayEnabled;

  public AccountController(AccountCatalog catalog,
      @Value("${course.demo-delay-enabled:false}") boolean demoDelayEnabled) {
    this.catalog = catalog;
    this.demoDelayEnabled = demoDelayEnabled;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Account> create(@RequestBody Mono<CreateAccountRequest> request) {
    return request.flatMap(catalog::create);
  }

  @GetMapping("/{id}")
  public Mono<Account> find(@PathVariable UUID id,
      @RequestParam(name = "delayMs", defaultValue = "0") long delayMs) {
    if (delayMs < 0 || delayMs > 3000 || (delayMs != 0 && !demoDelayEnabled)) {
      return Mono.error(new InvalidRequestException("delayMs solo se permite en dev (0 a 3000)"));
    }
    Mono<Account> result = catalog.find(id);
    // Demora NON BLOCKING para una simulacion de timeout en clase, no Thread.sleep().
    return delayMs == 0 ? result : Mono.delay(Duration.ofMillis(delayMs)).then(result);
  }
}
