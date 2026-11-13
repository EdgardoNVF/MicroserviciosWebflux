package com.bancolombia.training.transactions.service;

/** Account Service rechazo el Bearer transmitido (401). */
public class AccountServiceUnauthorizedException extends RuntimeException {
    public AccountServiceUnauthorizedException() {
        super("Account Service rechazo el token (401)");
    }
}
