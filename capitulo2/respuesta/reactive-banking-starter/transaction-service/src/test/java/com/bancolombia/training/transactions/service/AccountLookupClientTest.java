package com.bancolombia.training.transactions.service;

import java.time.Duration;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;

class AccountLookupClientTest {
  private static final UUID ACCOUNT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private static final String ACCOUNT_JSON = """
      {"accountId":"11111111-1111-4111-8111-111111111111", "holderName":"Cliente",
       "balance":"150000.00", "currency":"COP", "status":"ACTIVE",
       "createdAt":"2026-11-04T15:00:00Z"}
      """;

  @Test void propagaCorrelationIdYDevuelveCuenta() {
    AtomicReference<String> header = new AtomicReference<>();
    WebClient client = WebClient.builder().baseUrl("http://example.invalid")
        .exchangeFunction(request -> {
          header.set(request.headers().getFirst("X-Correlation-Id"));
          return Mono.just(ClientResponse.create(HttpStatus.OK)
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .body(ACCOUNT_JSON).build());
        }).build();
    StepVerifier.create(new AccountLookupClient(client, 500).findAccount(ACCOUNT_ID, "test-corr-01", 0))
        .assertNext(account -> assertEquals(ACCOUNT_ID, account.accountId())).verifyComplete();
    assertEquals("test-corr-01", header.get());
  }

  @Test void traduce404AlContrato() {
    WebClient client = WebClient.builder().baseUrl("http://example.invalid")
        .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build())).build();
    StepVerifier.create(new AccountLookupClient(client, 500).findAccount(ACCOUNT_ID, "test-corr-02", 0))
        .expectError(AccountNotFoundException.class).verify();
  }

  @Test void traduceTimeoutAIndisponibilidad() {
    WebClient client = WebClient.builder().baseUrl("http://example.invalid")
        .exchangeFunction(request -> Mono.delay(Duration.ofMillis(200))
            .thenReturn(ClientResponse.create(HttpStatus.OK).body(ACCOUNT_JSON).build())).build();
    StepVerifier.create(new AccountLookupClient(client, 20).findAccount(ACCOUNT_ID, "test-corr-03", 0))
        .expectError(AccountServiceUnavailableException.class).verify();
  }
  @Test void propagaBearerJwtPorContextoReactivo() {
    AtomicReference<String> authHeader = new AtomicReference<>();
    WebClient client = WebClient.builder()
        .baseUrl("http://example.invalid")
        .filter(new ServerBearerExchangeFilterFunction())
        .exchangeFunction(request -> {
          authHeader.set(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
          return Mono.just(ClientResponse.create(HttpStatus.OK)
              .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .body(ACCOUNT_JSON).build());
        }).build();

    Jwt jwt = Jwt.withTokenValue("jwt-de-prueba").header("alg", "RS256")
        .claim("sub", "operador")
        .claim("realm_access", Map.of("roles", List.of("ACCOUNT_READ")))
        .build();
    StepVerifier.create(new AccountLookupClient(client, 500)
        .findAccount(ACCOUNT_ID, "trace-123", 0)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new JwtAuthenticationToken(jwt))))
        .expectNextCount(1).verifyComplete();
    assertEquals("Bearer jwt-de-prueba", authHeader.get());
  }

  @Test void noConfundeForbiddenConIndisponibilidad() {
    WebClient client = WebClient.builder().baseUrl("http://example.invalid")
        .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.FORBIDDEN).build())).build();
    StepVerifier.create(new AccountLookupClient(client, 500).findAccount(ACCOUNT_ID, "trace-403", 0))
        .expectError(AccountServiceForbiddenException.class).verify();
  }

  @Test void noConfundeUnauthorizedConIndisponibilidad() {
    WebClient client = WebClient.builder().baseUrl("http://example.invalid")
        .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build())).build();
    StepVerifier.create(new AccountLookupClient(client, 500).findAccount(ACCOUNT_ID, "trace-401", 0))
        .expectError(AccountServiceUnauthorizedException.class).verify();
  }
}
