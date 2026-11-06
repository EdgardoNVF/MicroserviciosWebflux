package com.bancolombia.training.accounts.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StatusController {

    private final String environment;

    public StatusController(@Value("${course.environment:local}") String environment) {
        this.environment = environment;
    }

    @GetMapping("/internal/status")
    public Mono<ServiceStatus> status() {
        // TODO LAB01: sustituye los valores de demostración por "UP" y environment.
        // Conserva el retorno Mono y el nombre del servicio.
        return Mono.just(new ServiceStatus("account-service", "PENDING", "not-configured"));
    }
}
