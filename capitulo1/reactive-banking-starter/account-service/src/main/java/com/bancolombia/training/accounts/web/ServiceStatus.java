package com.bancolombia.training.accounts.web;

/** Respuesta técnica de la práctica. No es un indicador de salud de Actuator. */
public record ServiceStatus(String service, String status, String environment) {}
