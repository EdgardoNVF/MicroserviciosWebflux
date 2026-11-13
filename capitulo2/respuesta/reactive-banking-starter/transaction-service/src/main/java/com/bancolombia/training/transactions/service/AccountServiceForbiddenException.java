package com.bancolombia.training.transactions.service;

/** El token es valido pero no tiene permiso sobre Account Service (403). */
public class AccountServiceForbiddenException extends RuntimeException {
    public AccountServiceForbiddenException() {
        super("El usuario no tiene permisos en Account Service (403)");
    }
}
