package com.bancolombia.training.transactions.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import java.util.Arrays;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AccountClientConfig {
  @Bean
  WebClient accountWebClient(WebClient.Builder builder,
      @Value("${services.account.url}") String baseUrl, Environment environment) {
    WebClient.Builder configured = builder.baseUrl(baseUrl);

    if (Arrays.asList(environment.getActiveProfiles()).contains("lab03")) {
      configured.filter(new ServerBearerExchangeFilterFunction());
    }
    return configured.build();
  }
}
