package com.bancolombia.training.transactions.web;

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
        return Mono.just(new ServiceStatus("transaction-service", "UP", environment));
    }
}
