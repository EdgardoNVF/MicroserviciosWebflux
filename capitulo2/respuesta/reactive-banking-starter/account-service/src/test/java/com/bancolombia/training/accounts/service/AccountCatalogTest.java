package com.bancolombia.training.accounts.service;
import com.bancolombia.training.accounts.model.CreateAccountRequest;
import com.bancolombia.training.accounts.web.InvalidRequestException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;

class AccountCatalogTest {
  private final AccountCatalog catalog = new AccountCatalog();
  @Test void consultaCuentaSemilla() {
    StepVerifier.create(catalog.find(AccountCatalog.DEMO_ACCOUNT_ID))
        .assertNext(a -> {
          assertEquals("COP", a.currency());
          assertEquals("ACTIVE", a.status());
          assertEquals("150000.00", a.balance());
        }).verifyComplete();
  }
  @Test void creaCuentaConSaldoCero() {
    StepVerifier.create(catalog.create(new CreateAccountRequest("Cliente de Prueba", "COP")))
        .assertNext(a -> {
          assertNotNull(a.accountId());
          assertEquals("0.00", a.balance());
          assertEquals("ACTIVE", a.status());
        }).verifyComplete();
  }
  @Test void rechazaMonedaNoAdmitida() {
    StepVerifier.create(catalog.create(new CreateAccountRequest("Cliente", "USD")))
        .expectError(InvalidRequestException.class).verify();
  }
}
