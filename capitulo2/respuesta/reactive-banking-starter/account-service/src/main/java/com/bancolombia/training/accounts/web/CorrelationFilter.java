package com.bancolombia.training.accounts.web;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/** Este ID sirve para enlazar registros; no es una credencial de seguridad. */
@Component
public class CorrelationFilter implements WebFilter {
  private static final Logger log = LoggerFactory.getLogger(CorrelationFilter.class);
  public static final String HEADER = "X-Correlation-Id";
  public static final String ATTRIBUTE = "correlationId";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
    String correlationId = incoming != null && incoming.matches("[A-Za-z0-9._-]{1,64}")
        ? incoming : UUID.randomUUID().toString();
    exchange.getAttributes().put(ATTRIBUTE, correlationId);
    exchange.getResponse().getHeaders().set(HEADER, correlationId);
    log.info("request service=account-service method={} correlationId={}",
        exchange.getRequest().getMethod(), correlationId);
    return chain.filter(exchange);
  }
}
