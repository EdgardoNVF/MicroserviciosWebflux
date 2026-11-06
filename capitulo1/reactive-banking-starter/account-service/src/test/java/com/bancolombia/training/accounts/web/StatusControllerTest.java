package com.bancolombia.training.accounts.web;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusControllerTest {

    @Test
    void respondeConNombreEstadoYAmbiente() {
        StatusController controller = new StatusController("dev");

        StepVerifier.create(controller.status())
                .assertNext(response -> {
                    assertEquals("account-service", response.service());
                    assertEquals("UP", response.status());
                    assertEquals("dev", response.environment());
                })
                .verifyComplete();
    }
}
